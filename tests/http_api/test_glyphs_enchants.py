#!/usr/bin/env python3
"""
Glyphs + weapon enchantments migration battery: the 10 armor glyphs and 11
weapon enchantments are kind scripts (scripts/glyphs, scripts/enchants) on
kind-parameterized frameworks; DeferedDamage moved to scripts/buffs.

Covers: registry/weights, name+glow parity, per-kind proc effects (direct
defenceProc/proc probes with roll-gating trials; adjacency-dependent probes
re-place the target right inside the same lua chunk), one real-combat E2E,
legacy FQN nested-bundle restore, save round-trip.

Usage:
    RPD_HEADLESS_JAR=.../RemixedDungeon-Headless.jar python3 test_glyphs_enchants.py [--port 8094]
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

GLYPHS = ["Bounce", "Affection", "AntiEntropy", "Multiplicity", "Potential",
          "Metabolism", "Stench", "Viscosity", "Displacement", "Entanglement"]
ENCHANTS = ["Fire", "Poison", "Death", "Paralysis", "Leech", "Slow", "Swing",
            "Piercing", "Instability", "Horror", "Luck"]

# staged next to the hero, placed afresh inside every probe chunk: the game
# keeps ticking between HTTP calls and free-roaming rats break adjacency
STAGE = """
-- probes drain the hero (gas, electricity, deferred damage) - keep it alive
local hero = RPD.Dungeon.hero
if hero:ht() < 300 then hero:ht(300) end
hero:hp(hero:ht())

-- leftovers from earlier probes occupy the hero's neighborhood and break
-- adjacency / clone-cell staging - sweep them first
local function clearLeftovers()
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
clearLeftovers()

local function stageAdjacent()
    -- free cell within Chebyshev distance 3 of the hero (the immediate ring
    -- is often fully occupied by leftovers)
    local h = RPD.Dungeon.hero
    local lvl = RPD.Dungeon.level
    local W = lvl:getWidth()
    local origin = h:getPos()
    local ring = { 1, -1, W, -W, 1 + W, 1 - W, -1 + W, -1 - W }
    for pass = 1, 3 do
        local cells = {}
        if pass == 1 then
            cells = ring
        else
            for _, d1 in ipairs(ring) do
                for _, d2 in ipairs(ring) do
                    if pass == 2 then
                        table.insert(cells, d1 + d2)
                    else
                        for _, d3 in ipairs(ring) do
                            table.insert(cells, d1 + d2 + d3)
                        end
                    end
                end
            end
        end
        for _, off in ipairs(cells) do
            local try = origin + off
            if lvl:cellValid(try) and lvl.passable[try + 1] and not RPD.Actor:findChar(try) then
                return try
            end
        end
    end
    return -1
end
local function place(cell, state)
    local h = RPD.Dungeon.hero
    local lvl = RPD.Dungeon.level
    local mob = RPD.MobFactory:mobByName("%s")
    mob:setPos(cell)
    lvl:spawnMob(mob, 0)
    mob:setState(state or "HUNTING")
    return mob
end
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
        self.log_file = tempfile.mktemp(prefix="glyphs_", suffix=".log")
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


def test_registries(b: Battery) -> bool:
    ok = True
    r = b.lua_json("""
        local kinds = {}
        local pool = {}
        for i = 1, 400 do
            local g = RPD.Glyph:random()
            if g == nil then return json.encode({ fail = "nil glyph" }) end
            kinds[g:getEntityKind()] = 1
        end
        local n = 0
        for k, _ in pairs(kinds) do n = n + 1; pool[k] = 1 end
        local missing = {}
        for _, want in ipairs({"Bounce","Affection","AntiEntropy","Multiplicity","Potential",
                               "Metabolism","Stench","Viscosity","Displacement","Entanglement"}) do
            if not pool[want] then table.insert(missing, want) end
            if not RPD.Glyph:byKind(want) then table.insert(missing, want .. ":byKind") end
        end
        local ekinds = {}
        local epool = {}
        for i = 1, 600 do
            local e = RPD.Enchantment:random()
            if e == nil then return json.encode({ fail = "nil enchant" }) end
            ekinds[e:getEntityKind()] = 1
        end
        local en = 0
        for k, _ in pairs(ekinds) do en = en + 1; epool[k] = 1 end
        local emiss = {}
        for _, want in ipairs({"Fire","Poison","Death","Paralysis","Leech","Slow","Swing",
                               "Piercing","Instability","Horror","Luck"}) do
            if not epool[want] then table.insert(emiss, want) end
            if not RPD.Enchantment:byKind(want) then table.insert(emiss, want .. ":byKind") end
        end
        -- Fire/Poison weight 10 vs Death weight 1: Fire must land far more often
        local fire, death = 0, 0
        for i = 1, 800 do
            local k = RPD.Enchantment:random():getEntityKind()
            if k == "Fire" then fire = fire + 1 end
            if k == "Death" then death = death + 1 end
        end
        return json.encode({ glyphs = n, enchants = en,
                             missing = missing, emiss = emiss,
                             fire = fire, death = death })
    """)
    print("   registries:", r)
    ok &= r.get("glyphs") == 10
    ok &= r.get("enchants") == 11
    ok &= not r.get("missing")
    ok &= not r.get("emiss")
    ok &= r.get("fire", 0) > r.get("death", 999) * 3
    return ok


def test_name_glow_parity(b: Battery) -> bool:
    ok = True
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:level(20)
        local g = RPD.Glyph:byKind("Bounce")
        armor:inscribe(g)
        local nm = armor:name()
        armor:inscribe(nil)
        local plain = armor:name()
        local expected = RPD.JavaUtils:format(RPD.textById("Bounce_Txt"), { plain })
        armor:inscribe(g)
        local glow = g:glowing()
        -- weapon side
        local w = RPD.item("Quarterstaff")
        local e = RPD.Enchantment:byKind("Fire")
        w:enchant(e)
        local wn = w:name()
        w:enchant(nil)
        local wplain = w:name()
        local expectedW = RPD.JavaUtils:format(RPD.textById("Fire_Name_0"), { wplain })
        local eglow = e:glowing()
        return json.encode({ glyphName = nm, expected = expected,
                             plain = plain,
                             glowPeriod = glow.period,
                             weaponName = wn, weaponExpected = expectedW, weaponPlain = wplain,
                             eglowPeriod = eglow.period,
                             glyphColorR = math.floor(glow.red * 255 + 0.5),
                             enchantColorG = math.floor(eglow.green * 255 + 0.5) })
    """)
    print("   name/glow:", r)
    ok &= r.get("glyphName") == r.get("expected")
    ok &= r.get("glyphName") != r.get("plain")
    ok &= abs(float(r.get("glowPeriod", 0)) - 0.6) < 0.01  # Bounce: no glow -> Glowing.WHITE (period .6)
    ok &= int(r.get("glyphColorR", -1)) == 0xFF  # WHITE
    ok &= r.get("weaponName") == r.get("weaponExpected")
    ok &= r.get("weaponName") != r.get("weaponPlain")
    ok &= abs(float(r.get("eglowPeriod", 0)) - 1.0) < 0.01  # Glowing(color) default period
    ok &= int(r.get("enchantColorG", -1)) == 0x44  # Fire ORANGE 0xFF4400
    # a glowing glyph keeps its desc color (Stench GREEN 0x22CC44)
    r2 = b.lua_json("""
        local g = RPD.Glyph:byKind("Stench")
        local glow = g:glowing()
        return json.encode({ g = math.floor(glow.green * 255 + 0.5), r = math.floor(glow.red * 255 + 0.5) })
    """)
    print("   stench glow:", r2)
    ok &= int(r2.get("g", -1)) == 0xCC
    ok &= int(r2.get("r", -1)) == 0x22
    return ok


def test_glyph_procs(b: Battery) -> bool:
    ok = True

    # Bounce: attacker pushed away
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:level(20)
        armor:inscribe(RPD.Glyph:byKind("Bounce"))
        local moved = 0
        for i = 1, 20 do
            local cell = stageAdjacent()
            local mob = place(cell)
            armor.glyph:defenceProc(armor, mob, h, 3)
            if mob:getPos() ~= cell then moved = moved + 1 end
            mob:remove()
        end
        armor:inscribe(nil)
        return json.encode({ moved = moved })
    """)
    print("   Bounce:", r)
    ok &= r.get("moved", 0) > 0

    # Affection: attacker charmed
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:level(20)
        armor:inscribe(RPD.Glyph:byKind("Affection"))
        local charmed = false
        for i = 1, 12 do
            local cell = stageAdjacent()
            local mob = place(cell)
            armor.glyph:defenceProc(armor, mob, h, 3)
            if mob:hasBuff("Charm") then charmed = true end
            RPD.Buffs.Buff:detach(mob, "Charm")
            mob:remove()
        end
        armor:inscribe(nil)
        return json.encode({ charmed = charmed })
    """)
    print("   Affection:", r)
    ok &= r.get("charmed") is True

    # AntiEntropy: attacker frost
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:level(20)
        armor:inscribe(RPD.Glyph:byKind("AntiEntropy"))
        local frosted = false
        for i = 1, 12 do
            local cell = stageAdjacent()
            local mob = place(cell)
            armor.glyph:defenceProc(armor, mob, h, 3)
            if mob:hasBuff("Frost") then frosted = true end
            RPD.Buffs.Buff:detach(mob, "Frost")
            mob:remove()
        end
        armor:inscribe(nil)
        return json.encode({ frost = frosted })
    """)
    print("   AntiEntropy:", r)
    ok &= r.get("frost") is True

    # Stench: toxic gas forms at the attacker cell and drains over ticks.
    # Big-ht target: the gas formula is (ht + 5 + depth*5) / 40 per tick, so an
    # ht-8 rat takes 0-1 damage and would never register.
    r = b.lua_json(STAGE % "Golem" + """
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:level(20)
        armor:inscribe(RPD.Glyph:byKind("Stench"))
        local cell = stageAdjacent()
        local mob = place(cell, "PASSIVE")
        -- several procs: the glyph roll is ~84%/level-20, one call can miss
        for i = 1, 4 do
            armor.glyph:defenceProc(armor, mob, h, 3)
        end
        local id = mob:getId()
        armor:inscribe(nil)
        return json.encode({ id = id, cell = cell, ht = mob:ht(), hp0 = mob:hp() })
    """)
    print("   Stench staged:", r)
    b.client.wait_ticks(6)
    hurt = b.lua_json("""
        local mob = RPD.CharsList:getById(%d)
        if mob == nil or not mob:valid() then return json.encode({ hurt = true, gone = true }) end
        return json.encode({ hurt = mob:hp() < mob:ht() or mob:hasBuff("Poison"), gone = false })
    """ % int(r.get("id")))
    print("   Stench gas:", hurt)
    ok &= (hurt.get("hurt") is True) or (hurt.get("gone") is True)

    # Viscosity: damage deferred into DeferedDamage, drains 1/tick
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:level(20)
        armor:inscribe(RPD.Glyph:byKind("Viscosity"))
        local mob = place(stageAdjacent())
        -- the deferral roll is ~84% per proc; loop until it lands
        local ret, debuff, lvl = 7, nil, -1
        for i = 1, 8 do
            ret = armor.glyph:defenceProc(armor, mob, h, 7)
            debuff = h:buff("DeferedDamage")
            if debuff then lvl = debuff:level() break end
        end
        mob:remove()
        armor:inscribe(nil)
        return json.encode({ ret = ret, deferred = debuff ~= nil, lvl = lvl })
    """)
    print("   Viscosity:", r)
    ok &= r.get("ret") == 0
    ok &= r.get("deferred") is True
    ok &= int(r.get("lvl", -1)) >= 7
    b.client.wait_ticks(10)
    r2 = b.lua_json("""
        local h = RPD.Dungeon.hero
        return json.encode({ still = h:buff("DeferedDamage") ~= nil, drained = h:hp() < h:ht() })
    """)
    print("   Viscosity drain:", r2)
    ok &= r2.get("still") is False
    ok &= r2.get("drained") is True

    # Metabolism: heals a wounded hero
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:level(20)
        armor:inscribe(RPD.Glyph:byKind("Metabolism"))
        h:hp(math.floor(h:ht() / 3))
        local hp0 = h:hp()
        for i = 1, 12 do
            local mob = place(stageAdjacent())
            armor.glyph:defenceProc(armor, mob, h, 3)
            mob:remove()
        end
        armor:inscribe(nil)
        return json.encode({ hp0 = hp0, hp1 = h:hp() })
    """)
    print("   Metabolism:", r)
    ok &= int(r.get("hp1", 0)) > int(r.get("hp0", 0))

    # Multiplicity: hero clone appears
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:level(20)
        armor:inscribe(RPD.Glyph:byKind("Multiplicity"))
        local clones = 0
        for i = 1, 20 do
            h:hp(h:ht())
            local mob = place(stageAdjacent())
            armor.glyph:defenceProc(armor, mob, h, 3)
            mob:remove()
            local mobs = RPD.Dungeon.level:getMobs()
            for _, m in pairs(mobs) do
                if m:getEntityKind() == "MirrorImage" then clones = clones + 1 end
            end
        end
        armor:inscribe(nil)
        return json.encode({ clones = clones, hp = h:hp() })
    """)
    print("   Multiplicity:", r)
    ok &= int(r.get("clones", 0)) > 0
    # clean the clone
    b.lua("""
        local mobs = RPD.Dungeon.level:getMobs()
        for _, m in pairs(mobs) do
            if m:getEntityKind() == "MirrorImage" then m:remove() end
        end
    """)

    # Displacement: hero teleports off a hit (non-boss level)
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:inscribe(RPD.Glyph:byKind("Displacement"))
        local p0 = h:getPos()
        local moved = 0
        for i = 1, 25 do
            local mob = place(stageAdjacent())
            armor.glyph:defenceProc(armor, mob, h, 3)
            if h:getPos() ~= p0 then
                moved = moved + 1
                RPD.CharUtils:appear(h, p0)
            end
            mob:remove()
        end
        armor:inscribe(nil)
        return json.encode({ moved = moved })
    """)
    print("   Displacement:", r)
    ok &= r.get("moved", 0) > 0

    # Entanglement: roots + armor pool on the wearer
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:level(20)
        armor:inscribe(RPD.Glyph:byKind("Entanglement"))
        local roots = false
        for i = 1, 40 do
            local mob = place(stageAdjacent())
            armor.glyph:defenceProc(armor, mob, h, 3)
            mob:remove()
            if h:hasBuff("Roots") then roots = true end
        end
        local pool = h:buff("Armor")
        local poolLevel = pool and pool:level() or -1
        armor:inscribe(nil)
        RPD.Buffs.Buff:detach(h, "Roots")
        RPD.Buffs.Buff:detach(h, "Armor")
        return json.encode({ roots = roots, pool = pool ~= nil, poolLevel = poolLevel })
    """)
    print("   Entanglement:", r)
    ok &= r.get("roots") is True
    ok &= r.get("pool") is True
    ok &= int(r.get("poolLevel", -1)) >= 5

    # Potential: electricity damage to both sides
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:level(20)
        armor:inscribe(RPD.Glyph:byKind("Potential"))
        local fired = 0
        for i = 1, 16 do
            local mob = place(stageAdjacent())
            armor.glyph:defenceProc(armor, mob, h, 5)
            if mob:hp() < mob:ht() then fired = fired + 1 end
            mob:remove()
        end
        armor:inscribe(nil)
        return json.encode({ fired = fired, hpd = h:hp() < h:ht() })
    """)
    print("   Potential:", r)
    ok &= int(r.get("fired", 0)) > 0
    ok &= r.get("hpd") is True
    b.lua('RPD.Dungeon.hero:hp(RPD.Dungeon.hero:ht())')

    return ok


def test_glyph_real_combat(b: Battery) -> bool:
    """Real Char.attack path: mob hits glyphed hero, Stench gas lands.
    Golem target: the gas formula gives an ht-8 rat 0-1 damage per tick."""
    r = b.lua_json(STAGE % "Golem" + """
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:level(20)
        armor:inscribe(RPD.Glyph:byKind("Stench"))
        local cell = stageAdjacent()
        local mob = place(cell)
        mob:hp(mob:ht())
        return json.encode({ id = mob:getId(), hp0 = mob:hp() })
    """)
    tid = int(r["id"])
    for _ in range(4):  # the golem can dodge; each hit re-rolls the glyph proc
        b.client._get(f"/debug/force_attack?attacker={tid}&target=-1")
        time.sleep(0.5)
    # pin: a wandering defender steps off the gas cell before it ticks
    b.lua('local mob = RPD.CharsList:getById(%d) if mob and mob:valid() then mob:setState("PASSIVE") end' % tid)
    time.sleep(1)
    b.client.wait_ticks(6)
    r2 = b.lua_json("""
        local mob = RPD.CharsList:getById(%d)
        if mob == nil or not mob:valid() then return json.encode({ hurt = true, gone = true }) end
        return json.encode({ hurt = mob:hp() < mob:ht() or mob:hasBuff("Poison"), gone = false })
    """ % tid)
    print("   Stench E2E:", r, r2)
    b.lua('RPD.Dungeon.hero:getBelongings().armor:inscribe(nil)')
    return (r2.get("hurt") is True) or (r2.get("gone") is True)


def _equip_enchanted_staff(b: Battery, kind: str):
    b.lua("""
        local h = RPD.Dungeon.hero
        local w = h:getBelongings().weapon
        if w == nil or w:getEntityKind() ~= "Quarterstaff" then
            w = RPD.item("Quarterstaff")
            h:getBelongings():collect(w)
            h:getBelongings().weapon = w
        end
        w:level(20)
        w:enchant(RPD.Enchantment:byKind("%s"))
    """ % kind)


def _disarm(b: Battery):
    b.lua("""
        local h = RPD.Dungeon.hero
        local w = h:getBelongings().weapon
        if w ~= nil then w:enchant(nil) end
    """)


def test_enchant_procs(b: Battery) -> bool:
    ok = True

    # Poison
    _equip_enchanted_staff(b, "Poison")
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local w = h:getBelongings().weapon
        local poisoned = false
        for i = 1, 12 do
            local mob = place(stageAdjacent())
            w:attackProc(h, mob, 4)
            if mob:hasBuff("Poison") then poisoned = true end
            RPD.Buffs.Buff:detach(mob, "Poison")
            mob:remove()
        end
        return json.encode({ poisoned = poisoned })
    """)
    print("   Poison:", r)
    ok &= r.get("poisoned") is True
    _disarm(b)

    # Fire
    _equip_enchanted_staff(b, "Fire")
    r = b.lua_json(STAGE % "Golem" + """
        local h = RPD.Dungeon.hero
        local w = h:getBelongings().weapon
        local burned = false
        local hurt = false
        for i = 1, 12 do
            local mob = place(stageAdjacent())
            w:attackProc(h, mob, 4)
            if mob:hasBuff("Burning") then burned = true end
            if mob:hp() < mob:ht() then hurt = true end
            RPD.Buffs.Buff:detach(mob, "Burning")
            mob:remove()
        end
        return json.encode({ burned = burned, hurt = hurt })
    """)
    print("   Fire:", r)
    ok &= r.get("burned") is True
    ok &= r.get("hurt") is True
    _disarm(b)

    # Paralysis (Stun) / Slow / Horror (Terror)
    for kind, buffname, field in [("Paralysis", "Stun", "stunned"),
                                  ("Slow", "Slow", "slowed"),
                                  ("Horror", "Terror", "terror")]:
        _equip_enchanted_staff(b, kind)
        r = b.lua_json(STAGE % "Rat" + """
            local h = RPD.Dungeon.hero
            local w = h:getBelongings().weapon
            local hit = false
            for i = 1, 14 do
                local mob = place(stageAdjacent())
                w:attackProc(h, mob, 4)
                if mob:hasBuff("%s") then hit = true end
                RPD.Buffs.Buff:detach(mob, "%s")
                mob:remove()
            end
            return json.encode({ %s = hit })
        """ % (buffname, buffname, field))
        print("   %s:" % kind, r)
        ok &= r.get(field) is True
        _disarm(b)

    # Leech
    _equip_enchanted_staff(b, "Leech")
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local w = h:getBelongings().weapon
        h:hp(math.floor(h:ht() / 3))
        local hp0 = h:hp()
        for i = 1, 14 do
            local mob = place(stageAdjacent())
            w:attackProc(h, mob, 9)
            mob:remove()
        end
        return json.encode({ hp0 = hp0, hp1 = h:hp() })
    """)
    print("   Leech:", r)
    ok &= int(r.get("hp1", 0)) > int(r.get("hp0", 0))
    _disarm(b)

    # Luck: extra damage lands eventually
    _equip_enchanted_staff(b, "Luck")
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local w = h:getBelongings().weapon
        local hurt = false
        for i = 1, 20 do
            local mob = place(stageAdjacent())
            w:attackProc(h, mob, 2)
            if mob:hp() < mob:ht() then hurt = true end
            mob:remove()
        end
        return json.encode({ hurt = hurt })
    """)
    print("   Luck:", r)
    ok &= r.get("hurt") is True
    _disarm(b)

    # Swing: splash hits a second adjacent mob
    _equip_enchanted_staff(b, "Swing")
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local w = h:getBelongings().weapon
        local cell = stageAdjacent()
        local other = place(cell)
        local splash = 0
        for i = 1, 12 do
            local mob = place(stageAdjacent())
            other:hp(other:ht())
            w:attackProc(h, mob, 8)
            if other:hp() < other:ht() then splash = splash + 1 end
            mob:remove()
        end
        return json.encode({ splash = splash })
    """)
    print("   Swing:", r)
    ok &= int(r.get("splash", 0)) > 0
    _disarm(b)

    # Piercing: the char on the attack line behind the target is hit
    _equip_enchanted_staff(b, "Piercing")
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local w = h:getBelongings().weapon
        local lvl = RPD.Dungeon.level
        -- put the victim on the line through hero -> target -> victim
        local c1 = stageAdjacent()
        local d = c1 - h:getPos()
        local c2 = c1 + d
        local pierced = false
        if lvl:cellValid(c2) and not RPD.Actor:findChar(c2) and lvl.passable[c2 + 1] then
            local victim = place(c2)
            for i = 1, 12 do
                local mob = place(c1)
                victim:hp(victim:ht())
                w:attackProc(h, mob, 8)
                if victim:hp() < victim:ht() then pierced = true end
                mob:remove()
            end
        end
        return json.encode({ pierced = pierced })
    """)
    print("   Piercing:", r)
    ok &= r.get("pierced") is True
    _disarm(b)

    # Instability: fires (some effect lands) on a Boomerang
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local w = RPD.item("Boomerang")
        w:enchant(RPD.Enchantment:byKind("Instability"))
        local hurt = false
        for i = 1, 20 do
            local mob = place(stageAdjacent())
            w:attackProc(h, mob, 4)
            if mob:hp() < mob:ht() then hurt = true end
            mob:remove()
        end
        return json.encode({ hurt = hurt })
    """)
    print("   Instability:", r)
    ok &= r.get("hurt") is True
    _disarm(b)

    # Death: instant kill on the roll (throwaway mob)
    _equip_enchanted_staff(b, "Death")
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local w = h:getBelongings().weapon
        local killed = 0
        for i = 1, 30 do
            local mob = place(stageAdjacent())
            mob:hp(mob:ht())
            w:attackProc(h, mob, 2)
            if not mob:isAlive() then killed = killed + 1 end
            mob:remove()
        end
        return json.encode({ killed = killed })
    """)
    print("   Death:", r)
    ok &= int(r.get("killed", 0)) > 0
    _disarm(b)

    return ok


def test_enchant_real_combat(b: Battery) -> bool:
    """Hero swings a Poison-enchanted weapon via the debug attack endpoint."""
    _equip_enchanted_staff(b, "Poison")
    r = b.lua_json(STAGE % "Rat" + """
        local h = RPD.Dungeon.hero
        local cell = stageAdjacent()
        local mob = place(cell)
        mob:hp(mob:ht())
        return json.encode({ id = mob:getId(), cell = cell, hp0 = mob:hp() })
    """)
    tid = r["id"]
    b.client._get(f"/debug/force_attack?attacker=-1&target={int(tid)}")
    time.sleep(1)
    r2 = b.lua_json("""
        local mob = RPD.CharsList:getById(%d)
        if mob == nil or not mob:valid() then return json.encode({ gone = true }) end
        return json.encode({ poisoned = mob:hasBuff("Poison") == true,
                             gone = false, hurt = mob:hp() < mob:ht() })
    """ % tid)
    print("   Poison E2E:", r, r2)
    _disarm(b)
    return (r2.get("poisoned") is True) or (r2.get("gone") is True) or (r2.get("hurt") is True)


def test_legacy_saves(b: Battery) -> bool:
    ok = True
    r = b.lua_json("""
        local armor = RPD.fromLua({
            __className = "com.watabou.pixeldungeon.items.armor.ClothArmor",
            glyph = {
                __className = "com.watabou.pixeldungeon.items.armor.glyphs.Stench",
            },
        })
        local g = armor.glyph
        if g == nil then return json.encode({ fail = "no glyph" }) end
        local glow = g:glowing()
        return json.encode({ kind = g:getEntityKind(), system = g:getEntitySystem(),
                             period = glow.period,
                             colorG = math.floor(glow.green * 255 + 0.5) })
    """)
    print("   glyph legacy:", r)
    ok &= r.get("kind") == "Stench"
    ok &= r.get("system") == "glyph"
    ok &= int(r.get("colorG", -1)) == 0xCC

    r = b.lua_json("""
        local w = RPD.fromLua({
            __className = "com.watabou.pixeldungeon.items.weapon.melee.Quarterstaff",
            imbue = "NONE",
            enchantment = {
                __className = "com.watabou.pixeldungeon.items.weapon.enchantments.Fire",
            },
        })
        local e = w.enchantment
        if e == nil then return json.encode({ fail = "no enchant" }) end
        return json.encode({ kind = e:getEntityKind(),
                             colorR = math.floor(e:glowing().red * 255 + 0.5) })
    """)
    print("   enchant legacy:", r)
    ok &= r.get("kind") == "Fire"
    ok &= int(r.get("colorR", -1)) == 0xFF
    return ok


def test_save_roundtrip(b: Battery) -> bool:
    ok = True
    b.lua("""
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        armor:inscribe(RPD.Glyph:byKind("Viscosity"))
        local w = h:getBelongings().weapon
        if w == nil then
            w = RPD.item("Quarterstaff")
            h:getBelongings():collect(w)
            h:getBelongings().weapon = w
        end
        w:enchant(RPD.Enchantment:byKind("Luck"))
    """)
    b.client.reload_game()
    time.sleep(2)
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        local armor = h:getBelongings().armor
        local w = h:getBelongings().weapon
        local g = armor.glyph
        local e = w and w.enchantment or nil
        return json.encode({ glyph = g and g:getEntityKind() or "MISSING",
                             enchant = e and e:getEntityKind() or "MISSING",
                             system = g and g:getEntitySystem() or "?" })
    """)
    print("   save round-trip:", r)
    ok &= r.get("glyph") == "Viscosity"
    ok &= r.get("enchant") == "Luck"
    ok &= r.get("system") == "glyph"
    # and it round-trips AGAIN through the tagged form
    b.client.reload_game()
    time.sleep(2)
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        local g = h:getBelongings().armor.glyph
        local e = h:getBelongings().weapon and h:getBelongings().weapon.enchantment or nil
        return json.encode({ glyph = g and g:getEntityKind() or "MISSING",
                             enchant = e and e:getEntityKind() or "MISSING" })
    """)
    print("   re-save round-trip:", r)
    ok &= r.get("glyph") == "Viscosity"
    ok &= r.get("enchant") == "Luck"
    return ok


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", default="8094")
    args = parser.parse_args()
    port = int(args.port)

    b = Battery("localhost", port)
    if not b.start_server():
        return 1

    try:
        b.client.start_game("WARRIOR")
        time.sleep(2)
        b.run_test("registries", lambda: test_registries(b))
        b.run_test("name_glow_parity", lambda: test_name_glow_parity(b))
        b.run_test("glyph_procs", lambda: test_glyph_procs(b))
        b.run_test("glyph_real_combat", lambda: test_glyph_real_combat(b))
        b.run_test("enchant_procs", lambda: test_enchant_procs(b))
        b.run_test("enchant_real_combat", lambda: test_enchant_real_combat(b))
        b.run_test("legacy_saves", lambda: test_legacy_saves(b))
        b.run_test("save_roundtrip", lambda: test_save_roundtrip(b))
    finally:
        b.stop_server()

    print(f"\nPASS: {b.passed}  FAIL: {b.failed}")
    return 1 if b.failed else 0


if __name__ == "__main__":
    sys.exit(main())
