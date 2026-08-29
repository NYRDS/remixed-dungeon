#!/usr/bin/env python3
"""
Path traversal regression test (beads snap-37j).

The debug web server serves mod files via /fs/, /raw/, /web/pixelcraft/ and
writes via /upload, /api/save-json. Before the fix, ".." segments (raw or
percent-encoded) escaped the mod dirs: arbitrary file read on the host, and
traversal write of a .lua = code exec into the game process.

Every probe must come back 403. Legit endpoints (/list, /fs/ root listing,
root page) must keep working.

Run: python3 test_path_traversal.py --start-server
"""

import subprocess
import sys
import time
import os
import signal
import argparse
import tempfile
import http.client
from typing import Optional, List

import requests

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from game_client import GameClient
from log_monitor import LogMonitor


class TestRunner:
    def __init__(self, host: str = "localhost", port: int = 8080):
        self.host = host
        self.port = port
        self.client = GameClient(host, port)
        self.base_url = f"http://{host}:{port}"
        self.server_process: Optional[subprocess.Popen] = None
        self.log_file: Optional[str] = None
        self.log_monitor: Optional[LogMonitor] = None
        self.errors: List[str] = []
        self.passed = 0
        self.failed = 0

    def start_server(self) -> bool:
        print("STARTING GAME SERVER")
        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_root = os.path.dirname(os.path.dirname(script_dir))
        self.log_file = tempfile.mktemp(prefix="game_traversal_", suffix=".log")
        cmd = [
            "./gradlew",
            "-p",
            "RemixedDungeonDesktop",
            "runDesktopGameWithWebServer",
            "--args=--webserver=8080 --windowed",
        ]
        log_handle = open(self.log_file, "w")
        self.server_process = subprocess.Popen(
            cmd,
            cwd=project_root,
            stdout=log_handle,
            stderr=subprocess.STDOUT,
            preexec_fn=os.setsid,
        )
        self.log_monitor = LogMonitor(self.log_file)
        self.log_monitor.start()
        print("Waiting for server to start...", end="", flush=True)
        max_wait = 240
        start_time = time.time()
        while time.time() - start_time < max_wait:
            if self.client.check_server():
                print(" READY!")
                return True
            print(".", end="", flush=True)
            time.sleep(2)
        print(" TIMEOUT!")
        return False

    def stop_server(self):
        print("\nSTOPPING GAME SERVER")
        if self.log_monitor:
            self.log_monitor.stop()
            self.log_monitor = None
        if self.server_process:
            try:
                os.killpg(os.getpgid(self.server_process.pid), signal.SIGTERM)
                self.server_process.wait(timeout=10)
            except Exception:
                try:
                    os.killpg(os.getpgid(self.server_process.pid), signal.SIGKILL)
                except Exception:
                    pass
            self.server_process = None

    def _wait_for_game(self, timeout: int = 60) -> bool:
        print("Waiting for game init...", end="", flush=True)
        start = time.time()
        while time.time() - start < timeout:
            state = self.client.get_game_state()
            if "hero" in state:
                print(" READY!")
                return True
            print(".", end="", flush=True)
            time.sleep(1)
        print(" TIMEOUT!")
        return False

    # probes with RAW dot segments - sent via http.client, which does not
    # normalize the path. requests/urllib3 collapses ".." client-side, and we
    # want to prove the SERVER guard, not the client's politeness.
    RAW_DOT_PROBES = [
        "/fs/../../../etc/passwd",
        "/fs/../../../etc/passwd?download=1",
        "/raw/../../../etc/passwd",
        "/web/pixelcraft/../../../../etc/passwd",
    ]

    # percent-encoded probes reach the handler verbatim regardless of client
    ENCODED_PROBES = [
        "/fs/..%2F..%2F..%2Fetc%2Fpasswd",
        "/fs/..%2f..%2f..%2fetc%2fpasswd?download=1",
        "/raw/..%2F..%2F..%2Fetc%2Fpasswd",
        "/web/pixelcraft/..%2F..%2F..%2Fetc%2Fpasswd",
        "/edit-json?file=../../../etc/passwd",
        "/edit-lua?file=..%2F..%2Fetc%2Fpasswd",
        "/preview-image?file=../../../etc/passwd",
        "/api/get_texture?file=../../../etc/passwd",
        "/api/get_texture?file=..%2F..%2Fetc%2Fpasswd",
    ]

    def check(self, label: str, ok: bool, detail: str = ""):
        if ok:
            print(f"  PASS: {label}")
            self.passed += 1
        else:
            msg = f"{label} {detail}".rstrip()
            print(f"  FAIL: {msg}")
            self.errors.append(msg)
            self.failed += 1

    def run_probes(self):
        session = requests.Session()

        for probe in self.RAW_DOT_PROBES:
            try:
                conn = http.client.HTTPConnection(self.host, self.port, timeout=10)
                conn.putrequest("GET", probe, skip_host=False)
                conn.endheaders()
                resp = conn.getresponse()
                resp.read()
                status = resp.status
                conn.close()
                self.check(f"GET {probe}", status == 403, f"-> got {status}")
            except Exception as e:
                self.check(f"GET {probe}", False, f"-> exception {e}")

        for probe in self.ENCODED_PROBES:
            try:
                r = session.get(self.base_url + probe, timeout=10)
                self.check(f"GET {probe}", r.status_code == 403,
                           f"-> got {r.status_code}")
            except Exception as e:
                self.check(f"GET {probe}", False, f"-> exception {e}")

        # upload: multipart filename carries the traversal
        try:
            r = session.post(
                self.base_url + "/upload",
                files={"file": ("../../../pwned_traversal_test.lua", b"debug bomb")},
                data={"path": ""},
                timeout=10,
            )
            self.check("POST /upload traversal filename",
                       r.status_code == 403, f"-> got {r.status_code}")
        except Exception as e:
            self.check("POST /upload traversal filename", False, f"-> exception {e}")

        # upload: path param carries the traversal
        try:
            r = session.post(
                self.base_url + "/upload",
                files={"file": ("innocent.lua", b"debug bomb")},
                data={"path": "../../../"},
                timeout=10,
            )
            self.check("POST /upload traversal path", r.status_code == 403,
                       f"-> got {r.status_code}")
        except Exception as e:
            self.check("POST /upload traversal path", False, f"-> exception {e}")

        # save-json: filePath carries the traversal
        try:
            r = session.post(
                self.base_url + "/api/save-json",
                json={"filePath": "../../../pwned.json", "content": "{}"},
                timeout=10,
            )
            self.check("POST /api/save-json traversal", r.status_code == 403,
                       f"-> got {r.status_code}")
        except Exception as e:
            self.check("POST /api/save-json traversal", False, f"-> exception {e}")

    def run_positive_controls(self):
        session = requests.Session()

        r = session.get(self.base_url + "/", timeout=10)
        self.check("GET / (root page)", r.status_code == 200, f"-> got {r.status_code}")

        r = session.get(self.base_url + "/list", timeout=10)
        self.check("GET /list", r.status_code == 200, f"-> got {r.status_code}")

        # /fs/ with empty path = root listing, must stay alive
        r = session.get(self.base_url + "/fs/", timeout=10)
        self.check("GET /fs/ (root listing)", r.status_code == 200,
                   f"-> got {r.status_code}")

    def run_test(self):
        result = self.client.start_game("WARRIOR", 0)
        if "error" in result:
            print(f"  FAIL: could not start game: {result.get('error')}")
            self.failed += 1
            return
        if not self._wait_for_game():
            print("  FAIL: game did not initialize")
            self.failed += 1
            return

        # server 503s everything until the game is up - probes go after init
        self.run_positive_controls()
        self.run_probes()

    def run_all(self):
        self.run_test()

    def print_results(self):
        print("\n" + "=" * 60)
        print("RESULTS")
        print("=" * 60)
        for err in self.errors:
            print(f"ERROR: {err}")
        print(f"Passed: {self.passed}")
        print(f"Failed: {self.failed}")
        return self.failed == 0


def main():
    parser = argparse.ArgumentParser(description="Path traversal regression test")
    parser.add_argument("--start-server", action="store_true",
                        help="Start the game server automatically")
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=8080)
    args = parser.parse_args()

    runner = TestRunner(host=args.host, port=args.port)

    if args.start_server:
        if not runner.start_server():
            runner.stop_server()
            print("FAIL: server did not start")
            return 1

    try:
        runner.run_all()
    finally:
        if args.start_server:
            runner.stop_server()

    ok = runner.print_results()
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
