#!/usr/bin/env python3
"""
serverctl - operate the Remixed Dungeon debug game server.

Wraps the whole lifecycle that every test session hand-rolls:
start (background gradle run + log file + pidfile), wait for /ready,
wait for game init (start_game is async!), status, logs, stop, restart,
and a quick JSON GET for any debug endpoint.

Kills by pidfile process-group, never by pkill-from-a-shell-whose-own-
commandline-contains-the-port (that footgun killed this session twice).

Usage:
  serverctl.py start  [--port N] [--game CLASS] [--fresh-log] [--timeout S]
  serverctl.py stop   [--port N]
  serverctl.py restart [--port N] [--game CLASS]
  serverctl.py status [--port N]
  serverctl.py wait-ready [--port N] [--timeout S]
  serverctl.py wait-game [--port N] [--timeout S]
  serverctl.py logs   [--port N] [-n LINES] [-f]
  serverctl.py cmd    [--port N] /debug/observe

Examples:
  ./serverctl.py start --game WARRIOR          # up + game initialized
  ./serverctl.py cmd /debug/hero_status
  ./serverctl.py logs -n 50 -f
  ./serverctl.py stop
"""

import argparse
import json
import os
import signal
import subprocess
import sys
import time
from typing import Optional

import requests

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
LOG_DIR = "/tmp"


def log_path(port: int) -> str:
    return os.path.join(LOG_DIR, f"remixed-debug-{port}.log")


def pid_path(port: int) -> str:
    return os.path.join(LOG_DIR, f"remixed-debug-{port}.pid")


def base_url(port: int) -> str:
    return f"http://localhost:{port}"


def ready(port: int, timeout: float = 5) -> bool:
    try:
        r = requests.get(f"{base_url(port)}/ready", timeout=timeout)
        return r.status_code == 200 and r.json().get("ready", False)
    except Exception:
        return False


def game_initialized(port: int, timeout: float = 5) -> bool:
    try:
        r = requests.get(f"{base_url(port)}/debug/get_game_state", timeout=timeout)
        return "hero" in r.json()
    except Exception:
        return False


def pid_alive(pid: int) -> bool:
    try:
        os.kill(pid, 0)
        return True
    except OSError:
        return False


def running_pid(port: int) -> Optional[int]:
    pp = pid_path(port)
    if os.path.exists(pp):
        try:
            pid = int(open(pp).read().strip())
            if pid_alive(pid):
                return pid
        except (ValueError, OSError):
            pass
    return None


def find_server_pids(port: int) -> list:
    """java/gradle processes carrying --webserver=<port> on their cmdline."""
    pids = []
    marker = f"--webserver={port}".encode()
    for entry in os.listdir("/proc"):
        if not entry.isdigit():
            continue
        try:
            with open(f"/proc/{entry}/cmdline", "rb") as f:
                if marker in f.read():
                    pids.append(int(entry))
        except OSError:
            continue
    return pids


def do_start(args) -> int:
    port = args.port
    if running_pid(port) is not None or find_server_pids(port) or ready(port, timeout=2):
        print(f"already running on port {port} (pid {running_pid(port) or find_server_pids(port)}) - stop first or use restart")
        return 1

    if getattr(args, 'fresh_log', False) and os.path.exists(log_path(port)):
        os.remove(log_path(port))

    cmd = [
        "./gradlew", "-p", "RemixedDungeonDesktop",
        "runDesktopGameWithWebServer",
        f"--args=--webserver={port} --windowed",
    ]
    log_handle = open(log_path(port), "a")
    proc = subprocess.Popen(
        cmd, cwd=REPO_ROOT,
        stdout=log_handle, stderr=subprocess.STDOUT,
        preexec_fn=os.setsid,
    )
    with open(pid_path(port), "w") as f:
        f.write(str(proc.pid))
    print(f"starting on port {port}, pid {proc.pid}, log {log_path(port)}")

    if not do_wait_ready_impl(port, args.timeout):
        print("FAIL: server did not become ready - see `logs`")
        return 1

    if args.game:
        try:
            requests.get(f"{base_url(port)}/debug/start_game?class={args.game}&difficulty=0", timeout=10)
        except Exception as e:
            print(f"start_game request failed: {e}")
            return 1
        if not do_wait_game_impl(port, args.timeout):
            print("FAIL: game did not initialize - see `logs`")
            return 1
        st = requests.get(f"{base_url(port)}/debug/hero_status", timeout=10).json()
        print(f"game up: {st.get('levelId')} at ({st.get('x')},{st.get('y')})")
    return 0


def do_stop(args) -> int:
    port = args.port
    pid = running_pid(port)
    targets = set(find_server_pids(port))
    if pid is not None:
        # the tracked pid is the gradle wrapper; its process group covers the game jvm
        try:
            targets.update(int(p) for p in os.listdir("/proc") if p.isdigit()
                           and int(p) in (pid, os.getpgid(pid)))
            targets.add(os.getpgid(pid))
        except OSError:
            pass

    killed_any = False
    if pid is not None:
        try:
            os.killpg(os.getpgid(pid), signal.SIGTERM)
            killed_any = True
            for _ in range(10):
                if not pid_alive(pid):
                    break
                time.sleep(1)
            if pid_alive(pid):
                os.killpg(os.getpgid(pid), signal.SIGKILL)
        except OSError:
            pass

    # untracked servers (started outside this tool): SIGTERM the java procs directly
    for t in targets:
        if t == pid:
            continue
        try:
            os.kill(t, signal.SIGTERM)
            killed_any = True
        except OSError:
            pass
    if targets:
        for _ in range(8):
            if not find_server_pids(port):
                break
            time.sleep(1)
        for t in find_server_pids(port):
            try:
                os.kill(t, signal.SIGKILL)
                killed_any = True
            except OSError:
                pass

    pp = pid_path(port)
    if os.path.exists(pp):
        os.remove(pp)
    if killed_any:
        print(f"stopped (pid {pid}, {len(targets)} processes)")
        return 0
    print(f"nothing running on port {port}")
    return 0


def do_status(args) -> int:
    port = args.port
    pid = running_pid(port)
    print(f"port:     {port}")
    print(f"pid:      {pid if pid else '-'}")
    print(f"ready:    {ready(port)}")
    gi = game_initialized(port)
    print(f"game:     {gi}")
    if gi:
        try:
            st = requests.get(f"{base_url(port)}/debug/hero_status", timeout=10).json()
            print(f"hero:     {st.get('levelId')} ({st.get('x')},{st.get('y')}) hp {st.get('hp')}/{st.get('ht')} action={st.get('action')}")
        except Exception as e:
            print(f"hero:     unreadable ({e})")
    print(f"log:      {log_path(port)}")
    return 0


def do_wait_ready_impl(port: int, timeout: float) -> bool:
    t0 = time.time()
    while time.time() - t0 < timeout:
        if ready(port, timeout=3):
            return True
        time.sleep(2)
    return False


def do_wait_ready(args) -> int:
    ok = do_wait_ready_impl(args.port, args.timeout)
    print("ready" if ok else f"not ready after {args.timeout}s")
    return 0 if ok else 1


def do_wait_game_impl(port: int, timeout: float) -> bool:
    t0 = time.time()
    while time.time() - t0 < timeout:
        if game_initialized(port, timeout=3):
            return True
        time.sleep(1)
    return False


def do_wait_game(args) -> int:
    ok = do_wait_game_impl(args.port, args.timeout)
    print("game initialized" if ok else f"game not initialized after {args.timeout}s")
    return 0 if ok else 1


def do_logs(args) -> int:
    lp = log_path(args.port)
    if not os.path.exists(lp):
        print(f"no log at {lp}")
        return 1
    if args.follow:
        os.execvp("tail", ["tail", f"-n{args.lines}", "-f", lp])
    subprocess.run(["tail", f"-n{args.lines}", lp])
    return 0


def do_cmd(args) -> int:
    try:
        r = requests.get(f"{base_url(args.port)}{args.path}", timeout=20)
        try:
            print(json.dumps(r.json(), indent=2, ensure_ascii=False))
        except ValueError:
            print(f"HTTP {r.status_code}: {r.text[:500]}")
    except Exception as e:
        print(f"request failed: {e}")
        return 1
    return 0


def main() -> int:
    p = argparse.ArgumentParser(description="operate the Remixed Dungeon debug server")
    sub = p.add_subparsers(dest="cmd", required=True)

    def port_arg(sp):
        sp.add_argument("--port", type=int, default=8080)

    sp = sub.add_parser("start", help="start server in background and wait for ready")
    port_arg(sp)
    sp.add_argument("--game", default=None, help="hero class - also start a game and wait for init")
    sp.add_argument("--fresh-log", action="store_true", help="truncate the log first")
    sp.add_argument("--timeout", type=float, default=300)
    sp.set_defaults(fn=do_start)

    sp = sub.add_parser("stop", help="stop the server")
    port_arg(sp)
    sp.set_defaults(fn=do_stop)

    sp = sub.add_parser("restart", help="stop then start")
    port_arg(sp)
    sp.add_argument("--game", default=None)
    sp.add_argument("--fresh-log", action="store_true")
    sp.add_argument("--timeout", type=float, default=300)
    sp.set_defaults(fn=lambda a: (do_stop(a), do_start(a))[1])

    sp = sub.add_parser("status", help="show server/game state")
    port_arg(sp)
    sp.set_defaults(fn=do_status)

    sp = sub.add_parser("wait-ready", help="block until /ready")
    port_arg(sp)
    sp.add_argument("--timeout", type=float, default=300)
    sp.set_defaults(fn=do_wait_ready)

    sp = sub.add_parser("wait-game", help="block until game state has hero")
    port_arg(sp)
    sp.add_argument("--timeout", type=float, default=120)
    sp.set_defaults(fn=do_wait_game)

    sp = sub.add_parser("logs", help="tail the server log")
    port_arg(sp)
    sp.add_argument("-n", dest="lines", type=int, default=30)
    sp.add_argument("-f", dest="follow", action="store_true")
    sp.set_defaults(fn=do_logs)

    sp = sub.add_parser("cmd", help="GET a debug endpoint, pretty-print JSON")
    port_arg(sp)
    sp.add_argument("path")
    sp.set_defaults(fn=do_cmd)

    args = p.parse_args()
    return args.fn(args)


if __name__ == "__main__":
    sys.exit(main())
