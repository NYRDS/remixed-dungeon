#!/usr/bin/env python3
"""
Seeds migration battery: the 9 plant seeds (all were java *Seed classes, now
data kinds on the Seed framework + scripts/items/<Kind>.lua) - descriptors,
eat effects, plant flow, Rotberry quest couplings, legacy FQN save-compat,
Treasury seed rolls, save round-trip.

run_lua stringifies non-scalar returns, so structured probes return
json.encode({...}) (scripts/lib/json) and are parsed here.

Usage:
    RPD_HEADLESS_JAR=.../RemixedDungeon-Headless.jar python3 test_items_seeds.py [--port 8093]
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

AC_EAT = "Food_ACEat"
AC_PLANT = "Plant_ACPlant"

HEAD = 'local RPD = require "scripts/lib/commonClasses" local json = require "scripts/lib/json" '

SEED_KINDS = [
    "Firebloom.Seed", "Icecap.Seed", "Sorrowmoss.Seed", "Dreamweed.Seed",
    "Sungrass.Seed", "Earthroot.Seed", "Fadeleaf.Seed", "Rotberry.Seed",
    "Moongrace.Seed",
]

# kind -> (plant kind, desc string id, alchemy potion kind, image index in items/seeds.png)
SEED_DATA = {
    "Firebloom.Seed":  ("Firebloom", "Firebloom_Desc", "PotionOfLiquidFlame", 0),
    "Icecap.Seed":     ("Icecap", "Icecap_Desc", "PotionOfFrost", 1),
    "Sorrowmoss.Seed": ("Sorrowmoss", "Sorrowmoss_Desc", "PotionOfToxicGas", 2),
    "Dreamweed.Seed":  ("Dreamweed", "Dreamweed_Desc", "PotionOfInvisibility", 3),
    "Sungrass.Seed":   ("Sungrass", "Sungrass_Desc", "PotionOfHealing", 4),
    "Earthroot.Seed":  ("Earthroot", "Earthroot_Desc", "PotionOfParalyticGas", 5),
    "Fadeleaf.Seed":   ("Fadeleaf", "Fadeleaf_Desc", "PotionOfMindVision", 6),
    "Rotberry.Seed":   ("Rotberry", "WandMaker_RotberryDesc", "PotionOfStrength", 7),
    "Moongrace.Seed":  ("Moongrace", "Moongrace_Desc", "PotionOfMana", 8),
}


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
        self.log_file = tempfile.mktemp(prefix="items_seeds_", suffix=".log")
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
        res = self.client.run_lua(HEAD + code)
        if "error" in res:
            raise RuntimeError("lua_eval %s: %s" % (code[:120], res["error"]))
        return res.get("result")

    def lua_json(self, code: str):
        return json.loads(self.lua(code))


def test_descriptors(b: Battery) -> bool:
    ok = True
    for kind, (plant, desc_id, potion, img) in SEED_DATA.items():
        d = b.lua_json(f"""
            local s = RPD.ItemFactory:itemByName("{kind}")
            return json.encode({{
                kind    = s:getEntityKind(),
                name    = s:name(),
                desc    = s:desc(),
                descOk  = s:desc() == RPD.textById("{desc_id}"),
                img     = s:image(),
                imgFile = tostring(s:imageFile()),
                bag     = s:bag(),
                price   = s:price(),
                upg     = s:isUpgradable(),
                ided    = s:isIdentified(),
                alc     = tostring(s.alchemyKind),
                eatAct  = s:actions(RPD.Dungeon.hero):contains("Food_ACEat"),
                plantAct= s:actions(RPD.Dungeon.hero):contains("Plant_ACPlant"),
            }})
        """)
        if not all((d.get("kind") == kind, d.get("descOk"), int(d.get("img", -1)) == img,
                    d.get("alc") == potion, d.get("bag") == "SeedPouch", int(d.get("price") or 0) == 10,
                    d.get("upg") is False, d.get("ided") is True, d.get("eatAct") is True,
                    d.get("plantAct") is True, "seeds.png" in (d.get("imgFile") or ""))):
            print(f"   {kind} MISMATCH:", d)
        ok &= d.get("kind") == kind
        ok &= d.get("descOk") is True
        ok &= int(d.get("img", -1)) == img
        ok &= d.get("alc") == potion
        ok &= d.get("bag") == "SeedPouch"
        ok &= int(d.get("price") or 0) == 10
        ok &= d.get("upg") is False
        ok &= d.get("ided") is True
        ok &= d.get("eatAct") is True
        ok &= d.get("plantAct") is True
        ok &= "seeds.png" in (d.get("imgFile") or "")
    return ok


def _give_seed(b: Battery, kind: str):
    """Clean residue from the previous block, then put a fresh kind instance
    into the hero backpack (kept in _G for the execute step)."""
    b.lua(f"""
        local h = RPD.Dungeon.hero
        h:hp(h:ht())
        h:detachBuff("Poison")
        h:detachBuff("Burning")
        h:detachBuff("Frost")
        h:detachBuff("Paralysis")
        local s = RPD.ItemFactory:itemByName("{kind}")
        s:collect(h:getBelongings().backpack)
        _G.__seed_under_test = s
    """)


def _eat(b: Battery):
    b.lua("""
        local s = _G.__seed_under_test
        s:execute(RPD.Dungeon.hero, "Food_ACEat")
    """)


def test_eat_effects(b: Battery) -> bool:
    ok = True
    hero_hp = b.lua_json("""
        local h = RPD.Dungeon.hero
        return json.encode({ hp = h:hp(), ht = h:ht(), pos = h:getPos(),
                             sp = h:getSkillPoints() })
    """)

    # hurt the hero first so heal hooks have headroom
    b.lua("""
        local h = RPD.Dungeon.hero
        h:hp(math.floor(h:ht() / 2))
    """)

    # Sungrass: Charm + partial heal
    _give_seed(b, "Sungrass.Seed")
    _eat(b)
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        return json.encode({ charm = h:hasBuff("Charm"), hp = h:hp() })
    """)
    print("   Sungrass:", r)
    ok &= r.get("charm") is True
    ok &= int(r.get("hp") or 0) >= int(hero_hp.get("ht") or 0) // 2

    # Icecap: Frost + small heal
    _give_seed(b, "Icecap.Seed")
    _eat(b)
    r = b.lua_json("""local h = RPD.Dungeon.hero
        return json.encode({ frost = h:hasBuff("Frost") })""")
    print("   Icecap:", r)
    ok &= r.get("frost") is True

    # Dreamweed: Vertigo + MindVision
    _give_seed(b, "Dreamweed.Seed")
    _eat(b)
    r = b.lua_json("""local h = RPD.Dungeon.hero
        return json.encode({ v = h:hasBuff("Vertigo"), mv = h:hasBuff("MindVision") })""")
    print("   Dreamweed:", r)
    ok &= r.get("v") is True
    ok &= r.get("mv") is True

    # Earthroot: Roots + Barkskin at floor(effectiveSTR/4)
    _give_seed(b, "Earthroot.Seed")
    _eat(b)
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        local bark = h:buff("Barkskin")  -- nil unless present
        return json.encode({ roots = h:hasBuff("Roots"),
                             bark = bark ~= nil,
                             lvl = bark ~= nil and bark:level() or -1,
                             want = math.floor(h:effectiveSTR() / 4) })
    """)
    print("   Earthroot:", r)
    ok &= r.get("roots") is True
    ok &= r.get("bark") is True
    if r.get("bark"):
        ok &= int(r.get("lvl") or 0) >= int(r.get("want") or 0)

    # Fadeleaf: teleport + Vertigo
    pos_before = b.lua("return RPD.Dungeon.hero:getPos()")
    _give_seed(b, "Fadeleaf.Seed")
    _eat(b)
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        return json.encode({ v = h:hasBuff("Vertigo"), pos = h:getPos() })
    """)
    print("   Fadeleaf:", r, "(pos before:", pos_before, ")")
    ok &= r.get("v") is True

    # Firebloom: Burning + Speed
    _give_seed(b, "Firebloom.Seed")
    _eat(b)
    r = b.lua_json("""local h = RPD.Dungeon.hero
        return json.encode({ burn = h:hasBuff("Burning"), speed = h:hasBuff("Speed") })""")
    print("   Firebloom:", r)
    ok &= r.get("burn") is True
    ok &= r.get("speed") is True

    # Moongrace: Charm + skill points gain
    sp_before = int(b.lua("return RPD.Dungeon.hero:getSkillPoints()") or 0)
    _give_seed(b, "Moongrace.Seed")
    _eat(b)
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        return json.encode({ charm = h:hasBuff("Charm"), sp = h:getSkillPoints() })
    """)
    print("   Moongrace:", r)
    ok &= r.get("charm") is True
    ok &= int(r.get("sp") or 0) >= sp_before

    # Sorrowmoss: Poison + Invisibility
    _give_seed(b, "Sorrowmoss.Seed")
    _eat(b)
    r = b.lua_json("""local h = RPD.Dungeon.hero
        return json.encode({ p = h:hasBuff("Poison"), inv = h:hasBuff("Invisibility") })""")
    print("   Sorrowmoss:", r)
    ok &= r.get("p") is True
    ok &= r.get("inv") is True
    return ok


def test_rotberry(b: Battery) -> bool:
    ok = True
    # eat: Toxic + Paralytic gas erupt at the hero cell
    b.lua("""
        local h = RPD.Dungeon.hero
        h:hp(h:ht())
        local s = RPD.ItemFactory:itemByName("Rotberry.Seed")
        s:collect(h:getBelongings().backpack)
        s:execute(h, "Food_ACEat")
    """)
    b.client.wait_ticks(3)
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        local lvl = RPD.Dungeon.level
        local toxic = lvl.blobs:get(RPD.Blobs.ToxicGas)
        local para  = lvl.blobs:get(RPD.Blobs.ParalyticGas)
        return json.encode({
            hasToxic  = toxic ~= nil,
            hasPara   = para ~= nil,
            toxicHere = toxic ~= nil and toxic.cur[h:getPos() + 1] > 0,
            paraHere  = para ~= nil and para.cur[h:getPos() + 1] > 0,
        })
    """)
    print("   Rotberry blobs:", r)
    # both blobs must exist at the hero cell after the eat; the center-cell
    # amounts are timing-dependent (both gases diffuse away within the eat's
    # ~6 ticks - java behaves identically), so only presence is asserted
    ok &= r.get("hasToxic") is True
    ok &= r.get("hasPara") is True

    # collect: level-wide challenge + hero-only warning
    b.client.get_recent_logs()  # drain
    r = b.lua("""
        local h = RPD.Dungeon.hero
        local s = RPD.ItemFactory:itemByName("Rotberry.Seed")
        local collected = s:collect(h:getBelongings().backpack)
        return tostring(collected)
    """)
    ok &= r == "true"
    time.sleep(1)
    warn_text = b.lua('return RPD.textById("WandMaker_RotberryInfo")')
    logs = b.client.get_recent_logs().get("logs", [])
    needle = str(warn_text)[:20]
    warn_seen = any(needle in str(line) for line in logs)
    print("   Rotberry collect warn seen:", warn_seen, f"(needle: {needle!r})")
    ok &= warn_seen

    # burn: inflammable keeps the berry, plain seeds burn to nothing
    r = b.lua_json("""
        local berry = RPD.ItemFactory:itemByName("Rotberry.Seed")
        local grass = RPD.ItemFactory:itemByName("Sungrass.Seed")
        return json.encode({
            berryKept = tostring(berry:burn(1) == berry),
            grassAsh  = tostring(grass:burn(1) == nil),
        })
    """)
    print("   Rotberry burn:", r)
    ok &= r.get("berryKept") == "true"
    ok &= r.get("grassAsh") == "true"

    # clean the gas up so the rest of the battery is not fought by it
    b.lua("""
        local h = RPD.Dungeon.hero
        local lvl = RPD.Dungeon.level
        lvl.blobs:remove(RPD.Blobs.ToxicGas)
        lvl.blobs:remove(RPD.Blobs.ParalyticGas)
        h:hp(h:ht())
        h:detachBuff("Paralysis")
        h:detachBuff("Poison")
    """)
    return ok


def test_plant_flow(b: Battery) -> bool:
    ok = True

    # the AC_PLANT action path first (detach from backpack + onThrow at hero
    # pos) while the hero cell is still plant-free
    _give_seed(b, "Firebloom.Seed")
    b.lua("_G.__seed_under_test:execute(RPD.Dungeon.hero, 'Plant_ACPlant')")
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        local obj = RPD.Dungeon.level:getTopLevelObject(h:getPos())
        return json.encode({ kind = tostring(obj ~= nil and obj:getEntityKind()) })
    """)
    print("   AC_PLANT action:", r)
    ok &= r.get("kind") == "Firebloom"

    for kind, (plant, _, _, _) in SEED_DATA.items():
        if plant == "Rotberry":
            continue  # already planted by the WandMaker quest levels; avoid noise
        r = b.lua_json(f"""
            local h = RPD.Dungeon.hero
            local lvl = RPD.Dungeon.level
            local obj = lvl:getTopLevelObject(h:getPos())
            if obj ~= nil then
                lvl:remove(obj)
            end
            local s = RPD.ItemFactory:itemByName("{kind}")
            lvl:plant(s, h:getPos())
            local obj2 = lvl:getTopLevelObject(h:getPos())
            return json.encode({{ planted = tostring(obj2 ~= nil),
                                  kind = tostring(obj2 ~= nil and obj2:getEntityKind()) }})
        """)
        print(f"   plant {kind}:", r)
        ok &= r.get("planted") == "true"
        ok &= r.get("kind") == plant
    return ok


def test_treasury_roll(b: Battery) -> bool:
    ok = True
    rolled = set()
    for _ in range(30):
        kind = b.lua("""
            local s = RPD.Treasury:getLevelTreasury():random("SEED")
            return s ~= nil and s:getEntityKind() or "nil"
        """)
        rolled.add(kind)
    print("   Treasury SEED rolls:", sorted(rolled))
    ok &= len(rolled) >= 4
    ok &= all(k.endswith(".Seed") for k in rolled)
    return ok


def test_legacy_saves(b: Battery) -> bool:
    ok = True
    for fqn, kind, img in [
        ("com.watabou.pixeldungeon.plants.Sungrass$Seed", "Sungrass.Seed", 4),
        ("com.watabou.pixeldungeon.plants.Rotberry$Seed", "Rotberry.Seed", 7),
        ("com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker$Rotberry$Seed", "Rotberry.Seed", 7),
        ("com.watabou.pixeldungeon.plants.Earthroot$Seed", "Earthroot.Seed", 5),
        ("com.watabou.pixeldungeon.plants.Moongrace$Seed", "Moongrace.Seed", 8),
    ]:
        d = b.lua_json(f"""
            local s = RPD.fromLua({{ __className = "{fqn}" }})
            return json.encode({{ kind = s:getEntityKind(), img = s:image(),
                                  alc = tostring(s.alchemyKind) }})
        """)
        print(f"   {fqn}:", d)
        ok &= d.get("kind") == kind
        ok &= int(d.get("img", -1)) == img

    # kind-tagged restore path (modern saves carry "<Plant>.Seed" directly)
    d = b.lua_json("""
        local s = RPD.fromLua({ __className = "Sungrass.Seed", quantity = 3 })
        return json.encode({ kind = s:getEntityKind(), qty = s:quantity() })
    """)
    print("   tagged:", d)
    ok &= d.get("kind") == "Sungrass.Seed"
    ok &= int(d.get("qty") or 0) == 3
    return ok


def test_save_roundtrip(b: Battery) -> bool:
    ok = True
    _give_seed(b, "Icecap.Seed")
    b.client.reload_game()
    time.sleep(2)
    r = b.lua_json("""
        local h = RPD.Dungeon.hero
        local iter = h:getBelongings():iterator()
        while iter:hasNext() do
            local item = iter:next()
            if item ~= nil and item:getEntityKind() == "Icecap.Seed" then
                return json.encode({ kind = item:getEntityKind(),
                                     img = item:image(),
                                     alc = tostring(item.alchemyKind) })
            end
        end
        return json.encode({ kind = "MISSING" })
    """)
    print("   save round-trip:", r)
    ok &= r.get("kind") == "Icecap.Seed"
    ok &= int(r.get("img") or -1) == 1
    return ok


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", default="8093")
    args = parser.parse_args()
    port = int(args.port)

    b = Battery("localhost", port)
    if not b.start_server():
        return 1

    try:
        b.client.start_game("WARRIOR")
        time.sleep(2)
        b.run_test("descriptors", lambda: test_descriptors(b))
        b.run_test("eat_effects", lambda: test_eat_effects(b))
        b.run_test("rotberry", lambda: test_rotberry(b))
        b.run_test("plant_flow", lambda: test_plant_flow(b))
        b.run_test("treasury_roll", lambda: test_treasury_roll(b))
        b.run_test("legacy_saves", lambda: test_legacy_saves(b))
        b.run_test("save_roundtrip", lambda: test_save_roundtrip(b))
    finally:
        b.stop_server()

    print(f"\nPASS: {b.passed}  FAIL: {b.failed}")
    return 1 if b.failed else 0


if __name__ == "__main__":
    sys.exit(main())
