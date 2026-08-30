#!/usr/bin/env python3
"""
LLM control surface (beads snap-d75, snap-jc6, snap-vbu).

Agent driver loop endpoints:
- /debug/observe      - atomic world frame (hero, visible mobs/items, stairs)
- /debug/get_map      - grid dump (terrain rows, passable/visible/mapped masks)
- /debug/hero_status  - alive/pos/action poll (idle detection)
- /debug/move_to      - tap on a cell: walk / attack / pickup / descend,
                        resolved by the game's own tap logic (Hero.handle)

Also guards the exact-match router fix: /debug/cast_spell_on_target must
reach its own handler, not be shadowed by the /debug/cast_spell prefix.

Run: python3 test_llm_control.py --start-server
"""

import subprocess
import sys
import time
import os
import signal
import argparse
import tempfile
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
        self.log_file = tempfile.mktemp(prefix="game_llm_control_", suffix=".log")
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

    def check(self, label: str, ok: bool, detail: str = ""):
        if ok:
            print(f"  PASS: {label}")
            self.passed += 1
        else:
            msg = f"{label} {detail}".rstrip()
            print(f"  FAIL: {msg}")
            self.errors.append(msg)
            self.failed += 1

    def get(self, path: str):
        try:
            return requests.get(self.base_url + path, timeout=10).json()
        except Exception as e:
            return {"error": f"request failed: {e}"}

    def raw_get(self, path: str):
        try:
            return requests.get(self.base_url + path, timeout=10).json()
        except Exception as e:
            return {"error": f"request failed: {e}"}

    def test_hero_status_shape(self):
        status = self.get("/debug/hero_status")
        self.check("hero_status: 200 shape", "error" not in status, str(status))
        self.check("hero_status: alive", status.get("alive") is True, str(status))
        self.check("hero_status: hp>0", status.get("hp", 0) > 0, str(status))
        self.check("hero_status: has action", "action" in status, str(status))
        self.check("hero_status: levelId", bool(status.get("levelId")), str(status))
        return status

    def test_observe_shape(self):
        obs = self.get("/debug/observe")
        self.check("observe: no error", "error" not in obs, str(obs)[:200])
        hero = obs.get("hero", {})
        self.check("observe: hero hp", hero.get("hp", 0) > 0, str(hero)[:120])
        self.check("observe: hero gold is int", isinstance(hero.get("gold"), int), str(hero)[:120])
        self.check("observe: hero hunger", "hunger" in hero and "starving" in hero, str(hero)[:120])
        self.check("observe: hero buffs list", isinstance(hero.get("buffs"), list), str(hero)[:120])
        self.check("observe: mobs list", isinstance(obs.get("mobs"), list), str(obs)[:120])
        self.check("observe: items list", isinstance(obs.get("items"), list), str(obs)[:120])
        self.check("observe: exits list", isinstance(obs.get("exits"), list), str(obs)[:120])
        self.check("observe: entrance", isinstance(obs.get("entrance"), dict), str(obs)[:120])
        self.check("observe: levelId", bool(obs.get("levelId")), str(obs)[:120])

        # atomicity cross-check against the old endpoint
        state = self.client.get_game_state()
        self.check("observe: depth matches get_game_state",
                   obs.get("depth") == state.get("depth"),
                   f"observe={obs.get('depth')} state={state.get('depth')}")

        # mob entries carry the fields a driver needs
        for mob in obs.get("mobs", []):
            for field in ("id", "type", "x", "y", "hp", "dist", "state"):
                if field not in mob:
                    self.check(f"observe: mob entry field {field}", False, str(mob))
                    return obs
        self.check("observe: mob entries complete", True)
        return obs

    def test_get_map_shape(self, obs):
        m = self.get("/debug/get_map")
        self.check("get_map: no error", "error" not in m, str(m)[:200])
        w, h = m.get("width", 0), m.get("height", 0)
        terrain = m.get("terrain", [])
        self.check("get_map: row count == height", len(terrain) == h,
                   f"rows={len(terrain)} h={h}")
        self.check("get_map: row width == width",
                   all(len(r) == w for r in terrain), f"w={w}")
        self.check("get_map: passable mask rows",
                   all(len(r) == w and set(r) <= {"0", "1"} for r in m.get("passable", [])),
                   str(m.get("passable", []))[:80])
        self.check("get_map: visible mask rows",
                   all(len(r) == w for r in m.get("visible", [])), "")
        self.check("get_map: dims match observe",
                   (w, h) == (obs.get("width"), obs.get("height")),
                   f"map={w}x{h} observe={obs.get('width')}x{obs.get('height')}")
        entrance = m.get("entrance", {})
        self.check("get_map: entrance in bounds",
                   0 <= entrance.get("x", -1) < w and 0 <= entrance.get("y", -1) < h,
                   str(entrance))
        self.check("get_map: terrain values sane",
                   all(isinstance(v, int) and v >= 0 for r in terrain for v in r), "")

        masked = self.get("/debug/get_map?mask=1")
        mvals = [v for r in masked.get("terrain", []) for v in r]
        self.check("get_map: masked terrain >= -1", all(v >= -1 for v in mvals), "")
        # town start level is fully mapped - nothing hidden there. the real
        # mask check happens on a dungeon level (see test_mask_on_dungeon_level)
        return m

    def test_mask_on_dungeon_level(self):
        # safe/town levels are fully mapped by design - go to a real dungeon
        # level where fog of war exists. poll for arrival instead of sleeping.
        requests.get(self.base_url + "/debug/go_to_level?id=4", timeout=10)
        deadline = time.time() + 15
        level_id = None
        while time.time() < deadline:
            level_id = self.get("/debug/hero_status").get("levelId")
            if level_id == "4":
                break
            time.sleep(0.5)
        self.check("mask: arrived at dungeon level 4", level_id == "4",
                   f"levelId={level_id}")

        masked = self.get("/debug/get_map?mask=1")
        mvals = [v for r in masked.get("terrain", []) for v in r]
        hidden = sum(1 for v in mvals if v == -1)
        self.check("mask: hides unexplored cells on dungeon level", hidden > 0,
                   f"hidden={hidden} total={len(mvals)}")
        full = self.get("/debug/get_map")
        fvals = [v for r in full.get("terrain", []) for v in r]
        self.check("mask: unmasked returns full grid", all(v >= 0 for v in fvals),
                   f"negatives={sum(1 for v in fvals if v < 0)}")

    def test_hero_status_idle_poll(self, status):
        # pick a passable 4-neighbour from the map grid
        m = self.get("/debug/get_map")
        w = m["width"]
        passable = m["passable"]
        x, y = status["x"], status["y"]
        target = None
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < w and 0 <= ny < len(passable) and passable[ny][nx] == "1":
                target = (nx, ny)
                break
        if target is None:
            self.check("hero_status: neighbour cell found", False, "hero walled in?")
            return
        nx, ny = target

        move = self.client.move_hero(nx, ny)
        if not move.get("success"):
            self.check("hero_status: move accepted", False, str(move))
            return

        # poll instead of sleep - this is the whole point of the endpoint
        deadline = time.time() + 15
        final = None
        while time.time() < deadline:
            final = self.get("/debug/hero_status")
            if final.get("action") == "idle" or not final.get("alive"):
                break
            time.sleep(0.2)

        self.check("hero_status: settled to idle", final and final.get("action") == "idle",
                   str(final))
        self.check("hero_status: pos changed", final and (final["x"], final["y"]) == (nx, ny),
                   f"want {(nx, ny)} got {(final.get('x'), final.get('y')) if final else None}")

    def test_exact_router(self):
        # pre-fix, this could be shadowed by /debug/cast_spell depending on
        # HashMap iteration order - that handler ignores x/y and returns success
        r = self.raw_get("/debug/cast_spell_on_target?type=BloodTransfusion&x=-5&y=-5")
        self.check("router: cast_spell_on_target reaches own handler",
                   "Invalid coordinates" in str(r.get("error", r.get("message", ""))),
                   str(r))
        # unknown /debug path must NOT match a prefix anymore
        try:
            r = requests.get(self.base_url + "/debug/cast_spell_nonexistent", timeout=10)
            self.check("router: no prefix slop", r.status_code == 404, f"got {r.status_code}")
        except Exception as e:
            self.check("router: no prefix slop", False, f"exception {e}")

    def move_to(self, x, y):
        return self.raw_get(f"/debug/move_to?x={x}&y={y}")

    def poll_idle(self, timeout=30):
        deadline = time.time() + timeout
        final = None
        while time.time() < deadline:
            final = self.get("/debug/hero_status")
            if final.get("action") == "idle" or not final.get("alive"):
                break
            time.sleep(0.2)
        return final

    def test_move_to_walk(self, status):
        # straight-line reachable passable target: all cells on the segment passable
        m = self.get("/debug/get_map")
        w = m["width"]
        passable, visited, mapped = m["passable"], m["visible"], m["mapped"]

        def walkable(cx, cy):
            return (passable[cy][cx] == "1"
                    and (visited[cy][cx] == "1" or mapped[cy][cx] == "1"))

        x, y = status["x"], status["y"]
        target = None
        for d in (6, 5, 4, 3):
            for dx, dy in ((d, 0), (-d, 0), (0, d), (0, -d)):
                cells = [(x + dx * i // d, y + dy * i // d) for i in range(1, d + 1)]
                if all(0 <= cx < w and 0 <= cy < len(passable)
                       and walkable(cx, cy) for cx, cy in cells):
                    target = cells[-1]
                    break
            if target:
                break
        self.check("move_to: straight target found", target is not None)
        if not target:
            return
        tx, ty = target

        r = self.move_to(tx, ty)
        self.check("move_to: accepted", r.get("success") is True, str(r))
        self.check("move_to: resolved is Move", "Move" in str(r.get("resolved", "")),
                   str(r.get("resolved")))

        final = self.poll_idle(30)
        self.check("move_to: reached target", final and (final["x"], final["y"]) == (tx, ty),
                   f"want {(tx, ty)} got {(final.get('x'), final.get('y')) if final else None}")

    def test_move_to_attack(self, status):
        # hostile rat on an adjacent cell - tap must resolve to Attack
        x, y = status["x"], status["y"]
        resolved = None
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            cx, cy = x + dx, y + dy
            created = self.raw_get(f"/debug/create_mob?type=Rat&x={cx}&y={cy}")
            if not created.get("success"):
                continue
            r = self.move_to(cx, cy)
            resolved = str(r.get("resolved", ""))
            if "Attack" in resolved:
                break
            # rat wandered off - try another cell
        self.check("move_to: occupied by mob resolves to Attack",
                   resolved is not None and "Attack" in resolved, str(resolved))
        self.poll_idle(15)

    def test_move_to_pickup(self, status):
        x, y = status["x"], status["y"]
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            cx, cy = x + dx, y + dy
            made = self.raw_get(f"/debug/create_item?type=PotionOfHealing&x={cx}&y={cy}")
            if not made.get("success"):
                continue
            r = self.move_to(cx, cy)
            resolved = str(r.get("resolved", ""))
            if "PickUp" in resolved:
                self.check("move_to: occupied by item resolves to PickUp", True)
                self.poll_idle(15)
                return
        self.check("move_to: occupied by item resolves to PickUp", False,
                   "no free adjacent cell for the heap")

    @staticmethod
    def _bfs_reachable(passable, visited, mapped, w, h, start, goal):
        # mirror Hero.getCloser: walkable = passable && (visited || mapped)
        from collections import deque
        walkable = [[passable[y][x] == "1" and (visited[y][x] == "1" or mapped[y][x] == "1")
                     for x in range(w)] for y in range(h)]
        seen = {start}
        q = deque([start])
        while q:
            cur = q.popleft()
            if cur == goal:
                return True
            cx, cy = cur % w, cur // w
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = cx + dx, cy + dy
                n = ny * w + nx
                if 0 <= nx < w and 0 <= ny < h and n not in seen and walkable[ny][nx]:
                    seen.add(n)
                    q.append(n)
        return False

    def test_move_to_stairs(self):
        # last test - the level changes under our feet.
        # stairs may be unreachable from spawn on some levels (getCloser gives
        # up, hero idles - same as a human tapping an unreachable stair), so
        # try candidate levels and assert on the first one with a walkable exit.
        candidates = ["1", "4", "5", "6", "7", "8", "9", "10"]
        for lvl in candidates:
            self.raw_get(f"/debug/go_to_level?id={lvl}")
            deadline = time.time() + 15
            cur = None
            while time.time() < deadline:
                cur = self.get("/debug/hero_status").get("levelId")
                if cur == lvl:
                    break
                time.sleep(0.5)
            if cur != lvl:
                continue
            time.sleep(1)

            # an agent that knows its target stairs reveals the level first -
            # getCloser paths only through visited||mapped cells
            self.raw_get("/debug/reveal_map")
            time.sleep(1)

            # visible hostiles interrupt tap-walking - clear them like a
            # player would (fight through), so the walk can complete
            for mob in self.get("/debug/observe").get("mobs", []):
                self.raw_get(f"/debug/kill_mob?x={mob['x']}&y={mob['y']}")
            time.sleep(1)

            m = self.get("/debug/get_map")
            exits = m.get("exits", [])
            if not exits:
                continue
            w, h = m["width"], m["height"]
            hs = self.get("/debug/hero_status")
            start = hs["y"] * w + hs["x"]
            goal = exits[0]["y"] * w + exits[0]["x"]
            if not self._bfs_reachable(m["passable"], m["visible"], m["mapped"],
                                       w, h, start, goal):
                continue

            before = cur
            ex, ey = exits[0]["x"], exits[0]["y"]
            r = self.move_to(ex, ey)
            if "Descend" not in str(r.get("resolved", "")):
                continue  # tap resolved to something else - try next level

            # like a human: re-tap whenever the walk gets interrupted
            # (enemy spotted, pushable object) until the level changes
            deadline = time.time() + 90
            level_id = before
            retaps = 0
            while time.time() < deadline and level_id == before and retaps < 10:
                time.sleep(0.5)
                hs = self.get("/debug/hero_status")
                level_id = hs.get("levelId")
                if level_id != before:
                    break
                if hs.get("action") == "idle":
                    self.move_to(ex, ey)
                    retaps += 1

            if level_id != before:
                self.check("move_to: stairs resolves to Descend", True)
                self.check("move_to: descend changed level", True,
                           f"{before} -> {level_id} after {retaps} retaps")
                return

        self.check("move_to: stairs resolve to Descend and change level", False,
                   "no candidate level reached its stairs")

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

        time.sleep(1)
        self.test_exact_router()
        status = self.test_hero_status_shape()
        obs = self.test_observe_shape()
        self.test_get_map_shape(obs)
        self.test_hero_status_idle_poll(status)
        self.test_move_to_walk(status)
        self.test_move_to_attack(status)
        self.test_move_to_pickup(status)
        self.test_mask_on_dungeon_level()
        # stairs last - the level changes under our feet
        self.test_move_to_stairs()

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
    parser = argparse.ArgumentParser(description="LLM control surface slice 1 test")
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
