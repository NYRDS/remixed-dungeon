#!/usr/bin/env python3
"""
Blobs wave 2 battery: the wells (WaterOfHealth, WaterOfAwareness,
WaterOfTransmutation) + the alchemy pot (Alchemy) + WellWater compat, all
were java classes, now scripts/blobs/<Kind>.lua on ScriptBlob. Covers the
well affect() hero/heap flows, the transmutation swaps, the pot brew flow
(via the kind script and via the real pot.lua bump path), legacy FQN blob
restores and the save round-trip.

Usage:
    RPD_HEADLESS_JAR=.../RemixedDungeonHeadless-all.jar python3 test_blobs_wells.py [--port 8098]
"""

import argparse
import io
import json
import os
import contextlib
import signal
import subprocess
import sys
import tempfile
import time
from typing import Optional

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from game_client import GameClient
from log_monitor import LogMonitor
from headless_server import server_command, emit_ci_error

HEAD = 'local RPD = require "scripts/lib/commonClasses" local json = require "scripts/lib/json" '

WELL_KINDS = ["WellWater", "WaterOfHealth", "WaterOfAwareness",
              "WaterOfTransmutation", "Alchemy"]


class Battery:
    def __init__(self, host: str, port: int):
        self.host = host
        self.port = port
        self.client = GameClient(host, port)
        self.server_process: Optional[subprocess.Popen] = None
        self.log_file: Optional[str] = None
        self.log_monitor: Optional[LogMonitor] = None
        self.passed = 0
        self.failed = 0

    def start_server(self) -> bool:
        print("STARTING GAME SERVER")
        self.log_file = tempfile.mktemp(prefix="wells_", suffix=".log")
        cmd, workdir = server_command(str(self.port))
        log_handle = open(self.log_file, "w")
        self.server_process = subprocess.Popen(
            cmd, cwd=workdir, stdout=log_handle, stderr=subprocess.STDOUT,
            preexec_fn=os.setsid)
        self.log_monitor = LogMonitor(self.log_file)
        self.log_monitor.start()

        print("Waiting for server...", end="", flush=True)
        deadline = time.time() + 240
        while time.time() < deadline:
            if self.client.check_server():
                print(" READY!")
                return True
            print(".", end="", flush=True)
            time.sleep(2)
        print(" TIMEOUT!")
        emit_ci_error(self.log_file)
        return False

    def stop_server(self):
        if self.log_monitor:
            self.log_monitor.stop()
        if self.server_process:
            try:
                os.killpg(os.getpgid(self.server_process.pid), signal.SIGTERM)
                self.server_process.wait(timeout=5)
            except (subprocess.TimeoutExpired, ProcessLookupError):
                try:
                    os.killpg(os.getpgid(self.server_process.pid), signal.SIGKILL)
                except ProcessLookupError:
                    pass
            time.sleep(1)

    def run_test(self, name: str, func) -> bool:
        if self.log_monitor:
            self.log_monitor.set_test(name)
        try:
            buf = io.StringIO()
            with contextlib.redirect_stdout(buf):
                ok = func()
            if ok:
                self.passed += 1
                print(f"  PASS: {name}")
                return True
            self.failed += 1
            print(f"  FAIL: {name}")
            print(buf.getvalue())
            print("::error::FAIL %s" % name)
            return False
        except Exception as e:
            self.failed += 1
            print(f"  FAIL: {name} - Exception: {e}")
            print("::error::FAIL %s - Exception: %s" % (name, str(e)[:200]))
            return False
        finally:
            if self.log_monitor:
                self.log_monitor.set_test(None)

    def lua(self, code: str) -> str:
        if "scripts/lib/commonClasses" not in code:
            code = HEAD + code
        res = self.client.run_lua(code)
        if "error" in res:
            raise RuntimeError("lua_eval %s: %s" % (code[:160], res["error"]))
        return res.get("result")

    def lua_json(self, code: str):
        res = self.lua(HEAD + code)
        return json.loads(res)


def _stage_well(b: Battery, kind: str, cell_expr: str) -> str:
    """Seed a fresh well blob at the cell expression; returns the kind."""
    b.lua(f"""
        local lvl = RPD.Dungeon.level
        lvl:clearFrom("{kind}")
        local cell = {cell_expr}
        RPD.Blobs.Blob:seed(lvl, cell, 1, "{kind}")
    """)
    return kind


def _free_cell_lua() -> str:
    return """
local function freeCell()
    local h = RPD.Dungeon.hero
    local lvl = RPD.Dungeon.level
    local origin = h:getPos()
    -- full passable scan, at least 2 cells away from the hero
    for try = 0, lvl:getLength() - 1 do
        if lvl:cellValid(try) and lvl.passable[try + 1]
                and not RPD.Actor:findChar(try) and lvl:getHeap(try) == nil
                and lvl:distance(try, origin) >= 2 then
            return try
        end
    end
    return -1
end
"""


def test_kinds_registered(b: Battery) -> bool:
    ok = True
    r = b.lua_json(_free_cell_lua() + """
        local kinds = { "WellWater", "WaterOfHealth", "WaterOfAwareness",
                        "WaterOfTransmutation", "Alchemy" }
        local lvl = RPD.Dungeon.level
        local cell = freeCell()
        if cell < 0 then return json.encode({ fail = "no free cell" }) end
        local out = {}
        for i = 1, #kinds do
            local kind = kinds[i]
            lvl:clearFrom(kind)
            local blob = RPD.Blobs.Blob:seed(lvl, cell, 1, kind)
            out[kind] = { placed = lvl:blobAmountAt(kind, cell),
                          kind = blob:getEntityKind(),
                          system = blob:getEntitySystem(),
                          pos = blob:pos() }
            lvl:clearFrom(kind)
        end
        return json.encode(out)
    """)
    print("   registered:", json.dumps(r))
    for kind in WELL_KINDS:
        entry = r.get(kind) or {}
        ok &= entry.get("placed") == 1
        ok &= entry.get("kind") == kind
        ok &= entry.get("system") == "blob"
    return ok


def test_wellwater_swallow(b: Battery) -> bool:
    ok = True
    r = b.lua_json(_free_cell_lua() + """
        local lvl = RPD.Dungeon.level
        local cell = freeCell()
        if cell < 0 then return json.encode({ fail = "no free cell" }) end
        RPD.Blobs.Blob:seed(lvl, cell, 1, "WellWater")
        local heap = lvl:drop(RPD.item("Ration"), cell)
        local blob = lvl:getBlobByName("WellWater")
        local procced = blob:affect()
        local result = {
            procced = procced,
            heapGone = lvl:getHeap(cell) == nil,
            waterStays = lvl:blobAmountAt("WellWater", cell) > 0,
        }
        lvl:clearFrom("WellWater")
        return json.encode(result)
    """)
    print("   wellwater swallow:", r)
    ok &= r.get("procced") is False
    ok &= r.get("heapGone") is True
    ok &= r.get("waterStays") is True
    return ok


def test_health_well_hero(b: Battery) -> bool:
    ok = True
    b.lua("""
        local h = RPD.Dungeon.hero
        h:hp(1)
        local armor = h:getBelongings().armor
        if armor == nil then
            armor = RPD.item("ClothArmor")
            h:getBelongings():collect(armor)
            h:getBelongings().armor = armor
        end
        armor:setCursed(true)
    """)
    _stage_well(b, "WaterOfHealth", "RPD.Dungeon.hero:getPos()")
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        local lvl = RPD.Dungeon.level
        local blob = lvl:getBlobByName("WaterOfHealth")
        local procced = blob:affect()
        return json.encode({
            procced = procced,
            hpFull = h:hp() == h:ht(),
            uncursed = not h:getBelongings().armor:isCursed(),
            waterConsumed = lvl:blobAmountAt("WaterOfHealth", h:getPos()) == 0,
        })
    """)
    print("   health well hero:", r)
    ok &= r.get("procced") is True
    ok &= r.get("hpFull") is True
    ok &= r.get("uncursed") is True
    ok &= r.get("waterConsumed") is True
    return ok


def test_health_well_dewvial(b: Battery) -> bool:
    ok = True
    r = b.lua_json(_free_cell_lua() + """
        local lvl = RPD.Dungeon.level
        local cell = freeCell()
        if cell < 0 then return json.encode({ fail = "no free cell" }) end
        RPD.Blobs.Blob:seed(lvl, cell, 1, "WaterOfHealth")
        local vial = RPD.item("DewVial")
        lvl:drop(vial, cell)
        local blob = lvl:getBlobByName("WaterOfHealth")
        local procced = blob:affect()
        local heap = lvl:getHeap(cell)
        local full = heap ~= nil and heap:peek():itemBooleanHook("isFull", false)
        local result = {
            procced = procced,
            vialFull = full,
            waterConsumed = lvl:blobAmountAt("WaterOfHealth", cell) == 0,
        }
        lvl:clearFrom("WaterOfHealth")
        return json.encode(result)
    """)
    print("   health well dewvial:", r)
    ok &= r.get("procced") is True
    ok &= r.get("vialFull") is True
    ok &= r.get("waterConsumed") is True
    return ok


def test_health_well_junk(b: Battery) -> bool:
    ok = True
    r = b.lua_json(_free_cell_lua() + """
        local lvl = RPD.Dungeon.level
        local cell = freeCell()
        if cell < 0 then return json.encode({ fail = "no free cell" }) end
        RPD.Blobs.Blob:seed(lvl, cell, 1, "WaterOfHealth")
        lvl:drop(RPD.item("Ration"), cell)
        local blob = lvl:getBlobByName("WaterOfHealth")
        local procced = blob:affect()
        local result = {
            procced = procced,
            heapGone = lvl:getHeap(cell) == nil,
            waterStays = lvl:blobAmountAt("WaterOfHealth", cell) > 0,
        }
        lvl:clearFrom("WaterOfHealth")
        return json.encode(result)
    """)
    print("   health well junk:", r)
    ok &= r.get("procced") is False
    ok &= r.get("heapGone") is True
    ok &= r.get("waterStays") is True
    return ok


def test_awareness_well_hero(b: Battery) -> bool:
    ok = True
    # stage a secret door on a wall next to a passable cell (3-arg set -
    # luaj resolves the (cell, terrain) overload badly)
    secret = b.lua_json("""
        local lvl = RPD.Dungeon.level
        local h = RPD.Dungeon.hero
        for try = 0, lvl:getLength() - 1 do
            if lvl:cellValid(try) and lvl.map[try + 1] == RPD.Terrain.WALL
                    and lvl:distance(try, h:getPos()) < 12 then
                lvl:set(lvl:cellX(try), lvl:cellY(try), RPD.Terrain.SECRET_DOOR)
                return json.encode({ cell = try,
                                     discovered = RPD.Terrain:discover(RPD.Terrain.SECRET_DOOR) })
            end
        end
        return json.encode({ cell = -1 })
    """)
    if secret.get("cell", -1) < 0:
        print("   awareness well: no wall cell for the secret, skipped probe")
        return True

    _stage_well(b, "WaterOfAwareness", "RPD.Dungeon.hero:getPos()")
    r = b.lua_json(f"""
        local h = RPD.Dungeon.hero
        local lvl = RPD.Dungeon.level
        local blob = lvl:getBlobByName("WaterOfAwareness")
        local procced = blob:affect()
        local secretCell = {secret.get("cell")}
        local result = {{
            procced = procced,
            aware = h:hasBuff("Awareness"),
            secretDiscovered = lvl.map[secretCell + 1] == {secret.get("discovered")},
            waterConsumed = lvl:blobAmountAt("WaterOfAwareness", h:getPos()) == 0,
        }}
        return json.encode(result)
    """)
    print("   awareness well hero:", r)
    ok &= r.get("procced") is True
    ok &= r.get("aware") is True
    ok &= r.get("secretDiscovered") is True
    ok &= r.get("waterConsumed") is True
    return ok


def test_transmutation_well(b: Battery) -> bool:
    ok = True

    def probe(stage_expr):
        code = _free_cell_lua() + f"""
        local lvl = RPD.Dungeon.level
        local cell = freeCell()
        if cell < 0 then return json.encode({{ fail = "no free cell" }}) end
        lvl:clearFrom("WaterOfTransmutation")
        RPD.Blobs.Blob:seed(lvl, cell, 1, "WaterOfTransmutation")
        {stage_expr}
        local blob = lvl:getBlobByName("WaterOfTransmutation")
        local procced = blob:affect()
        local heap = lvl:getHeap(cell)
        local top = heap ~= nil and heap:peek() or nil
        local result = {{
            procced = procced,
            topKind = top and top:getEntityKind() or "NONE",
            topLevel = top and top:level() or -99,
            topLevelKnown = top and top:isLevelKnown() or false,
            waterConsumed = lvl:blobAmountAt("WaterOfTransmutation", cell) == 0,
            heapGone = heap == nil,
        }}
        lvl:clearFrom("WaterOfTransmutation")
        return json.encode(result)
        """
        return b.lua_json(code)

    # sword -> mace, upgrade level + known flag carried (loop the no-arg
    # upgrade - luaj resolves weapon:upgrade(n) to the boolean overload)
    r = probe("""
        local sword = RPD.item("Sword")
        for _ = 1, 2 do sword:upgrade() end
        sword:setLevelKnown(true)
        lvl:drop(sword, cell)
    """)
    print("   transmute sword:", r)
    ok &= r.get("procced") is True
    ok &= r.get("topKind") == "Mace"
    ok &= r.get("topLevel") == 2
    ok &= r.get("topLevelKnown") is True
    ok &= r.get("waterConsumed") is True

    # ScrollOfUpgrade <-> ScrollOfWeaponUpgrade
    r = probe("""
        lvl:drop(RPD.item("ScrollOfUpgrade"), cell)
    """)
    print("   transmute scroll:", r)
    ok &= r.get("procced") is True
    ok &= r.get("topKind") == "ScrollOfWeaponUpgrade"

    # PotionOfStrength -> PotionOfMight
    r = probe("""
        lvl:drop(RPD.item("PotionOfStrength"), cell)
    """)
    print("   transmute potion:", r)
    ok &= r.get("procced") is True
    ok &= r.get("topKind") == "PotionOfMight"

    # junk is rejected: the water stays, the heap is tossed
    r = probe("""
        lvl:drop(RPD.item("Ration"), cell)
    """)
    print("   transmute junk:", r)
    ok &= r.get("procced") is False
    ok &= r.get("heapGone") is True
    ok &= r.get("waterConsumed") is False

    return ok


def test_alchemy_pot_script(b: Battery) -> bool:
    ok = True
    # the brew probe: 3 stacked Sungrass seeds -> a potion (the alchemyKind
    # branch lands at p=2/3, the random-potion branch at p=1/3; retry until
    # the alchemyKind branch is seen)
    saw_brew = False
    saw_healing = False
    for attempt in range(14):
        r = b.lua_json(_free_cell_lua() + """
            local lvl = RPD.Dungeon.level
            local cell = freeCell()
            if cell < 0 then return json.encode({ fail = "no free cell" }) end
            local before = RPD.Statistics.potionsCooked
            local seeds = RPD.item("Sungrass.Seed")
            seeds:quantity(3)
            lvl:drop(seeds, cell)
            local alchemy = require "scripts/blobs/Alchemy"
            alchemy:affectCell(cell)
            local heap = lvl:getHeap(cell)
            local brewed = -1
            local W = lvl:getWidth()
            for _, off in ipairs({ 1, -1, W, -W }) do
                local try = cell + off
                if lvl:cellValid(try) and lvl:getHeap(try) ~= nil
                        and lvl:getHeap(try):peek():getEntityKind() ~= "Sungrass.Seed" then
                    brewed = lvl:getHeap(try):peek():getEntityKind()
                end
            end
            local result = {
                cookedDelta = RPD.Statistics.potionsCooked - before,
                heapGone = heap == nil,
                brewed = brewed,
            }
            return json.encode(result)
        """)
        if attempt == 0:
            print("   alchemy brew:", r)
        ok &= r.get("cookedDelta") == 1
        ok &= r.get("heapGone") is True
        ok &= r.get("brewed") not in (None, -1)
        if r.get("brewed") == "PotionOfHealing":
            saw_healing = True
        saw_brew = True
        if saw_healing:
            break
    ok &= saw_healing
    print(f"   alchemy alchemyKind branch seen: {saw_healing}")

    # a non-seed heap and a too-small seed count change nothing
    r = b.lua_json(_free_cell_lua() + """
        local lvl = RPD.Dungeon.level
        local cell = freeCell()
        if cell < 0 then return json.encode({ fail = "no free cell" }) end
        local before = RPD.Statistics.potionsCooked
        lvl:drop(RPD.item("Ration"), cell)
        local alchemy = require "scripts/blobs/Alchemy"
        alchemy:affectCell(cell)
        local junkKept = lvl:getHeap(cell) ~= nil
        lvl:getHeap(cell):destroy()
        local seeds = RPD.item("Sungrass.Seed")
        seeds:quantity(2)
        lvl:drop(seeds, cell)
        alchemy:affectCell(cell)
        local smallKept = lvl:getHeap(cell) ~= nil
        if smallKept then lvl:getHeap(cell):destroy() end
        local result = {
            counterQuiet = RPD.Statistics.potionsCooked == before,
            junkKept = junkKept,
            smallKept = smallKept,
        }
        return json.encode(result)
    """)
    print("   alchemy no-brew:", r)
    ok &= r.get("counterQuiet") is True
    ok &= r.get("junkKept") is True
    ok &= r.get("smallKept") is True
    return ok


def test_pot_bump_path(b: Battery) -> bool:
    """The real pot.lua bump flow - the line that was broken as
    '"Alchemy":transmute(pos)' in the wave-1 commit."""
    ok = True
    r = b.lua_json(_free_cell_lua() + """
        local lvl = RPD.Dungeon.level
        local cell = freeCell()
        if cell < 0 then return json.encode({ fail = "no free cell" }) end
        local before = RPD.Statistics.potionsCooked
        local seeds = RPD.item("Sungrass.Seed")
        seeds:quantity(4)
        lvl:drop(seeds, cell)
        local pot = require "scripts/objects/pot"
        local mockObject = { getPos = function() return cell end }
        pot.bump(pot, mockObject, {})
        local heap = lvl:getHeap(cell)
        local result = {
            cookedDelta = RPD.Statistics.potionsCooked - before,
            heapGone = heap == nil,
        }
        return json.encode(result)
    """)
    print("   pot bump:", r)
    ok &= r.get("cookedDelta") == 1
    ok &= r.get("heapGone") is True
    return ok


def test_legacy_fqn(b: Battery) -> bool:
    ok = True
    r = b.lua_json("""
        local out = {}
        for _, fqn in ipairs({
            "com.watabou.pixeldungeon.actors.blobs.WellWater",
            "com.watabou.pixeldungeon.actors.blobs.WaterOfHealth",
            "com.watabou.pixeldungeon.actors.blobs.WaterOfAwareness",
            "com.watabou.pixeldungeon.actors.blobs.WaterOfTransmutation",
            "com.watabou.pixeldungeon.actors.blobs.Alchemy",
        }) do
            local blob = RPD.fromLua({ __className = fqn })
            out[fqn] = { kind = blob:getEntityKind(), system = blob:getEntitySystem() }
        end
        return json.encode(out)
    """)
    print("   legacy FQN:", r)
    for fqn, entry in (r or {}).items():
        simple = fqn.rsplit(".", 1)[-1]
        ok &= entry.get("kind") == simple
        ok &= entry.get("system") == "blob"
    return ok


def test_save_roundtrip(b: Battery) -> bool:
    ok = True
    r1 = b.lua_json(_free_cell_lua() + """
        local lvl = RPD.Dungeon.level
        local cell = freeCell()
        if cell < 0 then return json.encode({ fail = "no free cell" }) end
        RPD.Blobs.Blob:seed(lvl, cell, 1, "WaterOfHealth")
        return json.encode({ cell = cell,
                             levelId = lvl.levelId,
                             well = lvl:blobAmountAt("WaterOfHealth", cell) })
    """)
    print("   roundtrip staged:", r1)
    ok &= r1.get("well") == 1

    # reload_game is the crash+restart path: the CURRENT level's unsaved
    # state is dropped, so force a level save by walking away first
    exits = b.client.get_exits()
    away = None
    for ex in exits.get("exits", []):
        if ex.get("id") != r1.get("levelId"):
            away = ex.get("id")
            break
    if away is None:
        print("   roundtrip: no away-exit, skipping reload probe")
        return ok

    b.client.descend_to(away)
    time.sleep(6)
    b.client.reload_game()
    time.sleep(4)

    r2 = b.lua_json("""
        local lvl = RPD.Dungeon.level
        local well = lvl:getBlobByName("WaterOfHealth")
        local result = {
            wellPresent = well ~= nil,
            wellPos = well ~= nil and well:pos() or -1,
            wellAmount = well ~= nil and lvl:blobAmountAt("WaterOfHealth", well:pos()) or -1,
            wellKind = well ~= nil and well:getEntityKind() or "MISSING",
        }
        return json.encode(result)
    """)
    print("   roundtrip reloaded:", r2)
    ok &= r2.get("wellPresent") is True
    ok &= r2.get("wellPos") == r1.get("cell")
    ok &= r2.get("wellAmount") == 1
    ok &= r2.get("wellKind") == "WaterOfHealth"

    # and the reloaded well still works end to end
    r3 = b.lua_json("""
        local h = RPD.Dungeon.hero
        local lvl = RPD.Dungeon.level
        local well = lvl:getBlobByName("WaterOfHealth")
        -- move the hero onto the well cell for the hero flow
        h:setPos(well:pos())
        h:hp(1)
        local procced = well:affect()
        return json.encode({ procced = procced,
                             hpFull = h:hp() == h:ht(),
                             consumed = lvl:blobAmountAt("WaterOfHealth", well:pos()) == 0 })
    """)
    print("   roundtrip drink:", r3)
    ok &= r3.get("procced") is True
    ok &= r3.get("hpFull") is True
    ok &= r3.get("consumed") is True
    return ok


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", default="8098")
    parser.add_argument("--host", default="localhost")
    args = parser.parse_args()

    b = Battery(args.host, int(args.port))

    if not b.start_server():
        b.stop_server()
        return 1

    try:
        b.client.start_game("WARRIOR")
        time.sleep(1)

        b.run_test("kinds_registered", lambda: test_kinds_registered(b))
        b.run_test("wellwater_swallow", lambda: test_wellwater_swallow(b))
        b.run_test("health_well_hero", lambda: test_health_well_hero(b))
        b.run_test("health_well_dewvial", lambda: test_health_well_dewvial(b))
        b.run_test("health_well_junk", lambda: test_health_well_junk(b))
        b.run_test("awareness_well_hero", lambda: test_awareness_well_hero(b))
        b.run_test("transmutation_well", lambda: test_transmutation_well(b))
        b.run_test("alchemy_pot_script", lambda: test_alchemy_pot_script(b))
        b.run_test("pot_bump_path", lambda: test_pot_bump_path(b))
        b.run_test("legacy_fqn", lambda: test_legacy_fqn(b))
        b.run_test("save_roundtrip", lambda: test_save_roundtrip(b))
    finally:
        b.stop_server()

    print(f"\n{b.passed} passed, {b.failed} failed")
    return 0 if b.failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
