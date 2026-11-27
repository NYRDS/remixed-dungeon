#!/usr/bin/env python3
"""
Item/quest-coupled buff migration battery: RageBuff, RatterAura,
RatKingAuraBuff, HeartOfDarknessBuff, OneWayLoveBuff, OneWayCursedLoveBuff,
Necrotism, Moongrace (all were java classes, now scripts/buffs/<Kind>.lua via
CustomBuff) + DeferedDamage (from the glyphs batch).

Usage:
    RPD_HEADLESS_JAR=.../RemixedDungeon-Headless.jar python3 test_item_buffs.py [--port 8097]
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

STAGE = """
local hero = RPD.Dungeon.hero
if hero:ht() < 300 then hero:ht(300) end
hero:hp(hero:ht())
local function stageAdjacent(kind)
    local h = RPD.Dungeon.hero
    local lvl = RPD.Dungeon.level
    local W = lvl:getWidth()
    local origin = h:getPos()
    local ring = { 1, -1, W, -W, 1 + W, 1 - W, -1 + W, -1 - W }
    for _, off in ipairs(ring) do
        local try = origin + off
        if lvl:cellValid(try) and lvl.passable[try + 1] and not RPD.Actor:findChar(try) then
            local mob = RPD.MobFactory:mobByName(kind or "Rat")
            mob:setPos(try)
            lvl:spawnMob(mob, 0)
            mob:setState("HUNTING")
            return mob
        end
    end
    return nil
end
local function clearRing()
    local h = RPD.Dungeon.hero
    local lvl = RPD.Dungeon.level
    local W = lvl:getWidth()
    local origin = h:getPos()
    local ring = { 0, 1, -1, W, -W, 1 + W, 1 - W, -1 + W, -1 - W }
    for _, off in ipairs(ring) do
        local mob = RPD.Actor:findChar(origin + off)
        if mob ~= nil and mob:valid() and mob:getEntityKind() ~= "Hero" then
            mob:remove()
        end
    end
end
clearRing()
"""


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
        self.log_file = tempfile.mktemp(prefix="itembuffs_", suffix=".log")
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


def test_attach_by_kind(b: Battery) -> bool:
    """attachArtifactBuff serves every migrated kind via the script scan."""
    ok = True
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        local kinds = { "RageBuff", "RatterAura", "RatKingAuraBuff", "HeartOfDarknessBuff",
                        "OneWayLoveBuff", "OneWayCursedLoveBuff" }
        local out = {}
        for _, kind in ipairs(kinds) do
            local item = RPD.item("DriedRose")
            RPD.ItemUtils:attachArtifactBuff(h, item, kind)
            local buff = h:buff(kind)
            out[kind] = buff ~= nil and buff:getEntityKind() or "MISSING"
            RPD.Buffs.Buff:detach(h, kind)
        end
        return json.encode(out)
    """)
    print("   attach:", r)
    for kind in ["RageBuff", "RatterAura", "RatKingAuraBuff", "HeartOfDarknessBuff",
                 "OneWayLoveBuff", "OneWayCursedLoveBuff"]:
        ok &= r.get(kind) == kind
    return ok


def test_rage_buff(b: Battery) -> bool:
    ok = True
    # attackProc doubling via the buff hook
    r = b.lua_json(STAGE + """
        local h = RPD.Dungeon.hero
        local item = RPD.item("DriedRose")
        RPD.ItemUtils:attachArtifactBuff(h, item, "RageBuff")
        local buff = h:buff("RageBuff")
        local doubled = buff:attackProc(h, nil, 5)
        return json.encode({ doubled = doubled, kind = buff:getEntityKind() })
    """)
    print("   rage:", r)
    ok &= int(r.get("doubled", 0)) == 10
    ok &= r.get("kind") == "RageBuff"
    # the sourced one survives ticks
    b.client.wait_ticks(3)
    r2 = b.lua_json("""
        local h = RPD.Dungeon.hero
        return json.encode({ sourced_sticks = h:buff("RageBuff") ~= nil })
    """)
    print("   rage sourced:", r2)
    ok &= r2.get("sourced_sticks") is True
    b.lua('RPD.Buffs.Buff:detach(RPD.Dungeon.hero, "RageBuff")')
    # bare permanentBuff on a clean hero: permanent() deactivates the actor,
    # so the unsourced java buff stayed inert forever - the lua one must too
    r3 = b.lua_json(STAGE + """
        local h = RPD.Dungeon.hero
        RPD.Buffs.Buff:permanent(h, "RageBuff")
        return json.encode({ attached = h:buff("RageBuff") ~= nil })
    """)
    b.client.wait_ticks(3)
    r4 = b.lua_json("""
        local h = RPD.Dungeon.hero
        return json.encode({ inert_stays = h:buff("RageBuff") ~= nil })
    """)
    print("   rage unsourced:", r3, r4)
    ok &= r4.get("inert_stays") is True
    b.lua('RPD.Buffs.Buff:detach(RPD.Dungeon.hero, "RageBuff")')
    return ok


def test_rat_king_aura(b: Battery) -> bool:
    ok = True
    r = b.lua_json(STAGE + """
        local h = RPD.Dungeon.hero
        local item = RPD.item("RatKingCrown")
        RPD.ItemUtils:attachArtifactBuff(h, item, "RatKingAuraBuff")
        local mob = stageAdjacent("Rat")
        local buff = h:buff("RatKingAuraBuff")
        buff:attackProc(h, mob, 4)
        return json.encode({ pet = mob:isPet(), kind = buff:getEntityKind() })
    """)
    print("   ratking:", r)
    ok &= r.get("pet") is True
    ok &= r.get("kind") == "RatKingAuraBuff"
    b.lua("""
        local mobs = RPD.Dungeon.level:getMobs()
        for _, m in pairs(mobs) do
            if m:isPet() then m:remove() end
        end
        RPD.Buffs.Buff:detach(RPD.Dungeon.hero, "RatKingAuraBuff")
    """)
    return ok


def test_heart_of_darkness(b: Battery) -> bool:
    ok = True
    r = b.lua_json(STAGE + """
        local h = RPD.Dungeon.hero
        local item = RPD.item("HeartOfDarkness")
        RPD.ItemUtils:attachArtifactBuff(h, item, "HeartOfDarknessBuff")
        local mob = stageAdjacent()
        local buff = h:buff("HeartOfDarknessBuff")
        buff:defenceProc(mob, h, 5)
        local spirits = 0
        local pets = 0
        local mobs = RPD.Dungeon.level:getMobs()
        for _, m in pairs(mobs) do
            if m:getEntityKind() == "SpiritOfPain" then
                spirits = spirits + 1
                if m:isPet() then pets = pets + 1 end
            end
        end
        return json.encode({ spirits = spirits, pets = pets })
    """)
    print("   heartofdarkness:", r)
    ok &= int(r.get("spirits", 0)) > 0
    ok &= int(r.get("pets", 0)) > 0
    b.lua("""
        local mobs = RPD.Dungeon.level:getMobs()
        for _, m in pairs(mobs) do
            if m:getEntityKind() == "SpiritOfPain" then m:remove() end
        end
        RPD.Buffs.Buff:detach(RPD.Dungeon.hero, "HeartOfDarknessBuff")
    """)
    return ok


def test_necrotism(b: Battery) -> bool:
    ok = True
    r = b.lua_json(STAGE + """
        local h = RPD.Dungeon.hero
        -- iteration via the buff level, remaining acts via script data
        local debuff = RPD.Buffs.Buff:affect(h, "Necrotism")
        debuff:level(3)
        local serpent = require "scripts/lib/serpent"
        debuff:loadScriptData(serpent.dump({ left = 6 }))
        -- a victim next to the hero to observe the spread
        local victim = stageAdjacent()
        return json.encode({ vid = victim:getId() })
    """)
    vid = int(r["vid"])
    b.client.wait_ticks(4)
    r2 = b.lua_json("""
        local h = RPD.Dungeon.hero
        local victim = RPD.CharsList:getById(%d)
        local v = { alive = victim ~= nil and victim:valid(),
                    infected = victim ~= nil and victim:valid() and victim:hasBuff("Necrotism"),
                    hp = victim and victim:valid() and victim:hp() or -1 }
        return json.encode({ heroHurt = h:hp() < h:ht(), still = h:buff("Necrotism") ~= nil,
                             victim = v })
    """ % vid)
    print("   necrotism:", r, r2)
    ok &= r2.get("heroHurt") is True
    victim = r2.get("victim", {})
    ok &= (victim.get("infected") is True) or (victim.get("alive") is False)
    b.lua("""
        RPD.Buffs.Buff:detach(RPD.Dungeon.hero, "Necrotism")
        local mobs = RPD.Dungeon.level:getMobs()
        for _, m in pairs(mobs) do
            if m:getEntityKind() ~= "Hero" then RPD.Buffs.Buff:detach(m, "Necrotism") end
        end
    """)
    return ok


def test_moongrace(b: Battery) -> bool:
    ok = True
    r = b.lua_json(STAGE + """
        local h = RPD.Dungeon.hero
        h:setSkillPoints(0)
        local debuff = RPD.affectBuff(h, "Moongrace")
        return json.encode({ attached = h:buff("Moongrace") ~= nil,
                             sp0 = h:getSkillPoints() })
    """)
    print("   moongrace:", r)
    ok &= r.get("attached") is True
    b.client.wait_ticks(7)
    r2 = b.lua_json("""
        local h = RPD.Dungeon.hero
        return json.encode({ sp1 = h:getSkillPoints(), still = h:buff("Moongrace") ~= nil })
    """)
    print("   moongrace after ticks:", r2)
    ok &= int(r2.get("sp1", 0)) > int(r.get("sp0", 0))
    # moving detaches (anchored cell)
    b.lua("""
        local h = RPD.Dungeon.hero
        RPD.CharUtils:appear(h, h:getPos() + 1)
    """)
    b.client.wait_ticks(7)
    r3 = b.lua_json("""
        local h = RPD.Dungeon.hero
        return json.encode({ still = h:buff("Moongrace") ~= nil })
    """)
    print("   moongrace after move:", r3)
    ok &= r3.get("still") is False
    return ok


def test_legacy_saves(b: Battery) -> bool:
    """Legacy FQN restore: the saved buff class FQN derives to the kind."""
    ok = True
    r = b.lua_json("""
        local one = RPD.fromLua({
            __className = "com.watabou.pixeldungeon.items.quest.CorpseDust$UndeadRageAuraBuff",
        })
        local two = RPD.fromLua({
            __className = "com.nyrds.pixeldungeon.mechanics.buffs.Necrotism",
            iteration = 3, left = 5,
        })
        return json.encode({
            rage = one ~= nil and one:getEntityKind() or "NIL",
            necro = two ~= nil and two:getEntityKind() or "NIL",
        })
    """)
    print("   legacy buffs:", r)
    ok &= r.get("rage") == "RageBuff"
    ok &= r.get("necro") == "Necrotism"
    return ok


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", default="8097")
    args = parser.parse_args()
    port = int(args.port)

    b = Battery("localhost", port)
    if not b.start_server():
        return 1

    try:
        b.client.start_game("WARRIOR")
        time.sleep(2)
        b.run_test("attach_by_kind", lambda: test_attach_by_kind(b))
        b.run_test("rage_buff", lambda: test_rage_buff(b))
        b.run_test("rat_king_aura", lambda: test_rat_king_aura(b))
        b.run_test("heart_of_darkness", lambda: test_heart_of_darkness(b))
        b.run_test("necrotism", lambda: test_necrotism(b))
        b.run_test("moongrace", lambda: test_moongrace(b))
        b.run_test("legacy_saves", lambda: test_legacy_saves(b))
    finally:
        b.stop_server()

    print(f"\nPASS: {b.passed}  FAIL: {b.failed}")
    return 1 if b.failed else 0


if __name__ == "__main__":
    sys.exit(main())
