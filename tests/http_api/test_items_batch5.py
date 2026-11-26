#!/usr/bin/env python3
"""
Items migration batch 5 battery: SpellBook, BlackSkull, BlackSkullOfMastery,
ChaosCrystal (all were java classes, now CustomItem scripts) + legacy FQN
save-compat.

run_lua stringifies non-scalar returns, so structured probes return
json.encode({...}) (scripts/lib/json) and are parsed here.

Usage:
    RPD_HEADLESS_JAR=.../RemixedDungeon-Headless.jar python3 test_items_batch5.py [--port 8092]
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

AC_READ = "Scroll_ACRead"
AC_NECROMANCY = "Necromancer_ACSpecial"
AC_USE = "SpiderCharm_Use"
AC_FUSE = "ChaosCrystal_Fuse"

HEAD = 'local RPD = require "scripts/lib/commonClasses" local json = require "scripts/lib/json" '


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
        self.log_file = tempfile.mktemp(prefix="items5_", suffix=".log")
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
        res = self.client.run_lua(code)
        if "error" in res:
            raise RuntimeError("lua_eval %s: %s" % (code[:120], res["error"]))
        return res.get("result")

    def lua_json(self, code: str):
        res = self.lua(HEAD + code)
        return json.loads(res)



def test_spellbook(b: Battery) -> bool:
    ok = True
    # 1. fresh book: kind, unidentified, price, artifact slot, ScrollHolder bag
    checks = b.lua_json("""
        local book = RPD.ItemFactory:itemByName("SpellBook")
        return json.encode({
          kind = book:getEntityKind(),
          identified = book:isIdentified(),
          price = book:price(),
          slot = tostring(book:slot(RPD.Dungeon.hero:getBelongings())),
          bag = book:bag(),
          upgradable = book:isUpgradable(),
        })
    """)
    print("   SpellBook fresh:", checks)
    ok &= checks.get("kind") == "SpellBook"
    ok &= checks.get("identified") is False
    ok &= int(checks.get("price") or 0) == 500
    ok &= "ARTIFACT" in checks.get("slot", "")
    ok &= checks.get("bag") == "ScrollHolder"
    ok &= checks.get("upgradable") is False

    # 2. java parity: burn returns nothing (fireproof)
    burn = b.lua(HEAD + """
        local book = RPD.ItemFactory:itemByName("SpellBook")
        return tostring(book:burn(RPD.Dungeon.hero:getPos()) == nil)
    """)
    print("   SpellBook burn:", burn)
    ok &= burn == "true"

    # 3. spell resolution: pre-set kind, resolve + persist, second resolve stable
    spell_checks = b.lua_json("""
        local book = RPD.ItemFactory:itemByName("SpellBook")
        book:itemHook("setSpellName", "MagicTorch")
        local raw = RPD.ItemUtils:spellBookSpell(book)
        local spell = RPD.ItemUtils:spellBookResolveSpell(book)
        local raw2 = RPD.ItemUtils:spellBookSpell(book)
        return json.encode({ raw = raw, resolved = spell:getEntityKind(), persisted = raw2 })
    """)
    print("   SpellBook spell:", spell_checks)
    ok &= spell_checks.get("raw") == "MagicTorch"
    ok &= spell_checks.get("resolved") == "MagicTorch"
    ok &= spell_checks.get("persisted") == "MagicTorch"

    # 4. empty spell rolls a random Common spell and persists it
    roll = b.lua(HEAD + """
        local book = RPD.ItemFactory:itemByName("SpellBook")
        local spell = RPD.ItemUtils:spellBookResolveSpell(book)
        return RPD.ItemUtils:spellBookSpell(book)
    """)
    print("   SpellBook rolled:", roll)
    ok &= bool(roll) and roll not in ("", "nil")

    # 5. read flow: equip + execute AC_READ -> UseSpell curAction -> cast ->
    #    self-identify (Spell.castCallback -> identifySourceBook)
    b.client.start_game("MAGE")
    time.sleep(2)
    b.client.give_item("SpellBook")
    time.sleep(1)
    ok &= b.lua(HEAD + """
        local hero = RPD.Dungeon.hero
        local book = hero:getBelongings():getItem("SpellBook")
        if book == nil then error("no SpellBook in backpack") end
        book:itemHook("setSpellName", "MagicTorch")
        book:doEquip(hero)
        local actions = book:actions(hero):toString()
        if not actions:find("Scroll_ACRead", 1, true) then
          error("AC_READ missing: " .. actions)
        end
        hero:setSkillPoints(99)
        book:execute(hero, "Scroll_ACRead")
        return "true"
    """) == "true"
    time.sleep(2)
    after = b.lua_json("""
        local hero = RPD.Dungeon.hero
        local book = hero:getBelongings():getItemFromSlot(RPD.Slot.ARTIFACT)
        local act = hero:getCurAction()
        return json.encode({
          equippedKind = book and book:getEntityKind() or "none",
          identified = book and book:isIdentified() or false,
          action = act and act:getClass():getSimpleName() or "none",
          hasLight = hero:hasBuff("Light"),
        })
    """)
    print("   SpellBook read:", after)
    ok &= after.get("equippedKind") == "SpellBook"
    ok &= after.get("identified") is True
    ok &= after.get("hasLight") is True

    # 6. identified book name/info carry the (localized) spell name
    named_ok = b.lua(HEAD + """
        local hero = RPD.Dungeon.hero
        local book = hero:getBelongings():getItemFromSlot(RPD.Slot.ARTIFACT)
        local spell = RPD.SpellFactory:getSpellByName("MagicTorch")
        local expectedName = string.format("%s (%s)", RPD.textById("SpellBook_Name"), spell:name())
        if book:name() ~= expectedName then
          error("name '" .. book:name() .. "' ~= '" .. expectedName .. "'")
        end
        if not book:desc():find(spell:name(), 1, true) then error("info lacks spell name") end
        return "true"
    """)
    print("   SpellBook name match:", named_ok)
    ok &= named_ok == "true"

    # 7. Treasury rolls SpellBook (the batch-4e loot regression)
    kinds = b.lua(HEAD + """
        local seen = {}
        for i = 1, 40 do
          local item = RPD.Treasury:get():random("UNIQUE")
          seen[item:getEntityKind()] = true
        end
        local list = {}
        for k in pairs(seen) do list[#list+1] = k end
        return table.concat(list, ",")
    """)
    print("   Treasury UNIQUE kinds over 40 rolls:", kinds)
    ok &= "SpellBook" in kinds
    return ok


def test_blackskull(b: Battery) -> bool:
    ok = True
    b.client.start_game("WARRIOR")
    time.sleep(2)

    # equip a fresh skull: identified, base image, artifact slot
    equip = b.lua_json("""
        local hero = RPD.Dungeon.hero
        local skull = RPD.ItemFactory:itemByName("BlackSkull")
        hero:collect(skull)
        skull:doEquip(hero)
        local equipped = hero:getBelongings():getItemFromSlot(RPD.Slot.ARTIFACT)
        return json.encode({
          kind = equipped and equipped:getEntityKind() or "none",
          id = equipped and equipped:isIdentified() or false,
          image = equipped and equipped:image() or -1,
        })
    """)
    print("   BlackSkull equip:", equip)
    ok &= equip.get("kind") == "BlackSkull"
    ok &= equip.get("id") is True
    ok &= equip.get("image") == 19

    def skull_state():
        return b.lua_json("""
            local hero = RPD.Dungeon.hero
            local skull = hero:getBelongings():getItemFromSlot(RPD.Slot.ARTIFACT)
            return json.encode({ kind = skull and skull:getEntityKind() or "none",
                                 image = skull and skull:image() or -1 })
        """)

    def pet_rats():
        return b.lua(HEAD + """
            local n = 0
            for _, m in pairs(RPD.Dungeon.level:getMobs()) do
              if m:getEntityKind() == "Rat" and m:isPet() then n = n + 1 end
            end
            return tostring(n)
        """)

    def pet_rats_wait(expected, seconds=3.0):
        deadline = time.time() + seconds
        while time.time() < deadline:
            if int(pet_rats()) >= expected:
                return int(pet_rats())
            time.sleep(0.5)
        return int(pet_rats())

    # 9 pettable deaths -> charging only (image stays 19)
    for i in range(9):
        mob = b.client.create_mob("Rat")
        time.sleep(0.3)
        b.client.kill_mob(mob_id=int(mob["id"]))
        time.sleep(0.3)
    state = skull_state()
    print("   BlackSkull after 9 deaths:", state)
    ok &= state.get("image") == 19

    # 10th death: awakens (image 20), the rat is resurrected
    mob = b.client.create_mob("Rat")
    rat_id = int(mob["id"])
    time.sleep(0.3)
    b.client.kill_mob(mob_id=rat_id)
    time.sleep(0.5)
    state = skull_state()
    pets_after = int(pet_rats())
    print("   BlackSkull awaken:", state, "pet rats:", pets_after)
    ok &= state.get("image") == 20
    # java parity: the AWAKENING death only flips the flag - no resurrect
    # happens until the next pettable death while activated
    ok &= pets_after == 0

    # 11th pettable death while activated: resurrects, spends the charge
    mob = b.client.create_mob("Rat")
    rat_id = int(mob["id"])
    time.sleep(0.3)
    b.client.kill_mob(mob_id=rat_id)
    time.sleep(0.5)
    state = skull_state()
    pets_after = pet_rats_wait(1)
    print("   BlackSkull resurrection:", state, "pet rats:", pets_after)
    ok &= state.get("image") == 19  # spent its charge, deactivated again
    ok &= pets_after == 1  # the activated death resurrected the rat as an undead pet

    # noResurrection mob (script flag): neither charges nor resurrects - the
    # skull stays uncharged (charge 0, deactivated, image 19)
    mob = b.client.create_mob("Rat")
    rid = int(mob["id"])
    time.sleep(0.3)
    flagged = b.lua(HEAD + f"""
        for _, m in pairs(RPD.Dungeon.level:getMobs()) do
          if m:getId() == {rid} then m.noResurrection = true return "true" end
        end
        return "false"
    """)
    assert flagged == "true", "flagging failed"
    b.client.kill_mob(mob_id=rid)
    time.sleep(0.5)
    state = skull_state()
    print("   BlackSkull noResurrection death:", state)
    ok &= state.get("image") == 19
    return ok


def test_blackskull_mastery(b: Battery) -> bool:
    ok = True
    b.client.start_game("NECROMANCER")
    time.sleep(2)

    # collect + actions carry the necromancy entry
    ok &= b.lua(HEAD + """
        local hero = RPD.Dungeon.hero
        local tome = RPD.ItemFactory:itemByName("BlackSkullOfMastery")
        tome:collect(hero)
        local tome = hero:getBelongings():getItem("BlackSkullOfMastery")
        if tome == nil then error("tome not collected") end
        local actions = tome:actions(hero):toString()
        if not actions:find("Necromancer_ACSpecial", 1, true) then
          error("AC_NECROMANCY missing: " .. actions)
        end
        return "true"
    """) == "true"

    # execute -> the LICH window (headless window machinery consumes it)
    ok &= b.lua(HEAD + """
        local hero = RPD.Dungeon.hero
        local tome = hero:getBelongings():getItem("BlackSkullOfMastery")
        tome:execute(hero, "Necromancer_ACSpecial")
        return "true"
    """) == "true"
    time.sleep(1)

    # the completion behind both buttons: MasteryItem.choose (java contract):
    # subclass set, LICH STR penalty, tome consumed
    choose = b.lua_json("""
        local hero = RPD.Dungeon.hero
        local tome = hero:getBelongings():getItem("BlackSkullOfMastery")
        local strBefore = hero:STR()
        local MasteryItem = luajava.bindClass("com.nyrds.pixeldungeon.items.common.MasteryItem")
        MasteryItem:choose(hero, tome, hero:getSubClassByName("LICH"))
        return json.encode({
          subClass = hero:getSubClass():name(),
          strDelta = hero:STR() - strBefore,
          tomeGone = hero:getBelongings():getItem("BlackSkullOfMastery") == nil,
        })
    """)
    print("   BSoM choose:", choose)
    ok &= choose.get("subClass") == "LICH"
    ok &= choose.get("strDelta") == -2
    ok &= choose.get("tomeGone") is True
    return ok


def test_chaos_crystal(b: Battery) -> bool:
    ok = True
    b.client.start_game("WARRIOR")
    time.sleep(2)

    fresh = b.lua_json("""
        local hero = RPD.Dungeon.hero
        local c = RPD.ItemFactory:itemByName("ChaosCrystal")
        hero:collect(c)
        return json.encode({ kind = c:getEntityKind(),
                             actions = c:actions(hero):toString(),
                             text = c:getText(), id = c:isIdentified(),
                             color = c:getColor() })
    """)
    print("   ChaosCrystal fresh:", fresh)
    ok &= fresh.get("kind") == "ChaosCrystal"
    ok &= AC_USE not in fresh.get("actions", "")
    ok &= fresh.get("text") is None
    ok &= fresh.get("id") is False
    ok &= fresh.get("color") == 0xe0a0a0

    # charge + identify state machine (java quirk: level 3+ reverts to unknown)
    states = b.lua_json("""
        local hero = RPD.Dungeon.hero
        local c = hero:getBelongings():getItem("ChaosCrystal")
        local function snapshot()
          return {
            actions = c:actions(hero):toString(),
            text    = c:getText(),
            name    = c:name(),
            id      = c:isIdentified(),
          }
        end
        for i = 1, 60 do c:ownerTakesDamage(1) end
        local charged = snapshot()
        c:identify()
        local lvl1 = snapshot()
        c:identify()
        local lvl2 = snapshot()
        c:identify()
        local lvl3 = snapshot()
        return json.encode({ charged = charged, lvl1 = lvl1, lvl2 = lvl2, lvl3 = lvl3 })
    """)
    for k in ("charged", "lvl1", "lvl2", "lvl3"):
        print("   ChaosCrystal", k, ":", states[k])
    ok &= AC_USE not in states["charged"]["actions"]  # identifyLevel 0 gates USE
    ok &= states["charged"].get("text") is None       # ...and hides the counter
    ok &= AC_USE in states["lvl1"]["actions"]
    ok &= AC_FUSE not in states["lvl1"]["actions"]  # 50+ needs identifyLevel>1
    ok &= states["lvl2"]["id"] is True
    ok &= AC_FUSE in states["lvl2"]["actions"]
    ok &= states["lvl3"]["id"] is False  # the java quirk

    # USE: real cell selection through the pending CellSelector
    ok &= b.lua(HEAD + """
        local hero = RPD.Dungeon.hero
        local c = hero:getBelongings():getItem("ChaosCrystal")
        c:ownerTakesDamage(1)
        local blobsBefore = RPD.Dungeon.level.blobs:size()
        c:execute(hero, "SpiderCharm_Use")
        RPD.GameScene:handleCell(hero:getPos() + 1)
        local blobsAfter = RPD.Dungeon.level.blobs:size()
        if blobsAfter == blobsBefore then error("no chaos blob seeded") end
        if c:getText() ~= "0/100" then error("charge not reset: " .. tostring(c:getText())) end
        return "true"
    """) == "true"
    print("   ChaosCrystal use: ok")

    # cancel path: charge kept (java listener spent only hero time)
    ok &= b.lua(HEAD + """
        local hero = RPD.Dungeon.hero
        local c = hero:getBelongings():getItem("ChaosCrystal")
        for i = 1, 5 do c:ownerTakesDamage(1) end
        if c:getText() ~= "5/100" then error("setup: " .. tostring(c:getText())) end
        c:execute(hero, "SpiderCharm_Use")
        RPD.GameScene:cancelCellSelector()
        if c:getText() ~= "5/100" then error("charge lost on cancel: " .. tostring(c:getText())) end
        return "true"
    """) == "true"
    print("   ChaosCrystal cancel: ok")

    # type routing per family (java bridge, lua-agnostic)
    ok &= b.lua(HEAD + """
        local function fuseExpect(kind, expect)
          local fused = RPD.ItemUtils:chaosFuseResult(RPD.ItemFactory:itemByName(kind))
          local got = fused and fused:getEntityKind() or "nil"
          if got ~= expect then error(kind .. " -> " .. got) end
        end
        fuseExpect("ScrollOfIdentify", "ScrollOfWeaponUpgrade")
        fuseExpect("WoodenBow", "ChaosBow")
        fuseExpect("Sword", "ChaosBlade")
        fuseExpect("LeatherArmor", "ChaosArmor")
        fuseExpect("WandOfMagicMissile", "ChaosStaff")
        fuseExpect("Ration", "nil")
        return "true"
    """) == "true"
    print("   ChaosCrystal fuse routing: ok")

    # full lua callback: consumes crystal + source, collects the product
    ok &= b.lua(HEAD + """
        local hero = RPD.Dungeon.hero
        local c = hero:getBelongings():getItem("ChaosCrystal")
        if c == nil then error("crystal gone from backpack") end
        c:execute(hero, "ChaosCrystal_Fuse")
        local cbId = #_G.ItemSelectionCallbacks
        local scroll = RPD.ItemFactory:itemByName("ScrollOfIdentify")
        hero:collect(scroll)
        _G.ItemSelectionCallbacks[cbId](scroll, hero)
        if hero:getBelongings():getItem("ScrollOfWeaponUpgrade") == nil then
          error("fused scroll not collected")
        end
        if hero:getBelongings():getItem("ChaosCrystal") ~= nil then
          error("crystal not consumed")
        end
        return "true"
    """) == "true"
    print("   ChaosCrystal fuse callback: ok")

    # legacy FQN save-compat
    legacy = b.lua_json("""
        local c = RPD.fromLua({
            __className = "com.nyrds.pixeldungeon.items.chaos.ChaosCrystal",
            charge = 77, identifyLevel = 2,
        })
        return json.encode({ kind = c:getEntityKind(), text = c:getText(),
                             id = c:isIdentified() })
    """)
    print("   ChaosCrystal legacy:", legacy)
    ok &= legacy.get("kind") == "ChaosCrystal"
    ok &= legacy.get("text") == "77/100"
    ok &= legacy.get("id") is True
    return ok


def test_legacy_saves(b: Battery) -> bool:
    ok = True
    b.client.start_game("WARRIOR")
    time.sleep(2)

    sb = b.lua_json("""
        local item = RPD.fromLua({
            __className = "com.nyrds.pixeldungeon.items.artifacts.SpellBook",
            spell = "Healing",
        })
        return json.encode({ kind = item:getEntityKind(),
                             spell = RPD.ItemUtils:spellBookSpell(item) })
    """)
    print("   SpellBook legacy:", sb)
    ok &= sb.get("kind") == "SpellBook"
    ok &= sb.get("spell") == "Healing"

    bs = b.lua_json("""
        local item = RPD.fromLua({
            __className = "com.nyrds.pixeldungeon.items.necropolis.BlackSkull",
            activated = true, charge = 4,
        })
        return json.encode({ kind = item:getEntityKind(),
                             image = item:image(), name = item:name() })
    """)
    print("   BlackSkull legacy:", bs)
    ok &= bs.get("kind") == "BlackSkull"
    ok &= bs.get("image") == 20

    bm = b.lua_json("""
        local item = RPD.fromLua({
            __className = "com.nyrds.pixeldungeon.items.necropolis.BlackSkullOfMastery",
        })
        return json.encode({ kind = item:getEntityKind(), id = item:isIdentified() })
    """)
    print("   BSoM legacy:", bm)
    ok &= bm.get("kind") == "BlackSkullOfMastery"
    ok &= bm.get("id") is True
    return ok


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", default="8092")
    args = parser.parse_args()
    port = int(args.port)

    b = Battery("localhost", port)
    if not b.start_server():
        b.stop_server()
        return 1

    try:
        b.run_test("spellbook", lambda: test_spellbook(b))
        b.run_test("blackskull", lambda: test_blackskull(b))
        b.run_test("blackskull_mastery", lambda: test_blackskull_mastery(b))
        b.run_test("chaos_crystal", lambda: test_chaos_crystal(b))
        b.run_test("legacy_saves", lambda: test_legacy_saves(b))
    finally:
        b.stop_server()

    print(f"\nRESULT: {b.passed} passed, {b.failed} failed")
    return 0 if b.failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
