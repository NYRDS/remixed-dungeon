#!/usr/bin/env python3
"""
retro_from_modern.py - recreate retro-format hero sprites from the modern set.

The modern hero system draws 32x32 frames whose declared visual box
(hero_modern/spritesDesc/Hero.json: visualOffset 8,8 / visualSize 14x17)
is exactly the retro frame size (14x17). The character occupies the same
relative area in both systems, so a faithful conversion is:

    composite-free per-layer crop of each modern sheet at the visual box,
    repacked into retro-geometry sheets (512x32, 14x17 cells, 36 per row).

Converted layers are the ones RetroHeroSpriteDef renders:
body, collar, head, hair, facial_hair, armor, helmet, death.
Modern-only layers (held items, shoulders, boots, accessories) have
no retro counterpart and are skipped.

Two retro-specific corrections are applied:
- bare-hand layers (body/hands/<bodyType>_none_*.png) are baked into
  the converted body - modern stores hands separately, retro bakes
  them into the body sheet;
- frames 14-18 (the die animation) are cleared on every converted
  layer and the death layer is copied from the hand-drawn retro set -
  modern keeps a falling body there and its own death sheet is too
  sparse for retro's 5-frame wisp.

Usage:
  tools/retro_from_modern.py convert --class WARRIOR --subclass NONE \
      --armor cloth --armor plate --out generated/retro
  tools/retro_from_modern.py all --out generated/retro
  tools/retro_from_modern.py preview --class MAGE --subclass WARLOCK \
      --armor cloth --out /tmp/preview.png   # generated vs original strip

Outputs never touch assets/ unless --into-assets is given.
Requires Pillow.
"""
import argparse
import json
import os
import shutil
import sys
from PIL import Image

ASSETS = os.path.join(os.path.dirname(__file__), "..", "RemixedDungeon", "src", "main", "assets")
MODERN = "hero_modern/"
RETRO = "hero/"

BODY_TYPES = ("man", "woman", "warlock", "lich", "gnoll")
FEMININE_CLASSES = {"HUNTRESS"}          # HeroClass.getGender()
CLASS_BODY = {"GNOLL": "gnoll"}          # body_types map in spritesDesc
SUBCLASS_BODY = {"WARLOCK": "warlock", "LICH": "lich"}

CLASSES = {
    "WARRIOR": ["NONE", "GLADIATOR", "BERSERKER"],
    "MAGE": ["NONE", "WARLOCK", "BATTLEMAGE"],
    "ROGUE": ["NONE", "ASSASSIN", "FREERUNNER"],
    "HUNTRESS": ["NONE", "SNIPER", "WARDEN"],
    "ELF": ["NONE", "SCOUT", "SHAMAN"],
    "NECROMANCER": ["NONE", "LICH"],
    "GNOLL": ["NONE", "GUARDIAN", "WITCHDOCTOR"],
    "PRIEST": ["NONE", "CLERIC", "PALADIN"],
    "DOCTOR": ["NONE", "ALCHEMIST", "TRANSMUTER"],
}

ARMORS = [
    "ClothArmor", "LeatherArmor", "MailArmor", "ScaleArmor", "PlateArmor",
    "GothicArmor", "RogueArmor", "WarriorArmor", "MageArmor", "HuntressArmor",
    "ScoutArmor", "ShamanArmor", "GladiatorArmor", "BerserkArmor",
    "WarlockArmor", "BattleMageArmor", "AssasinArmor", "FreeRunnerArmor",
    "SniperArmor", "WardenArmor", "NecromancerArmor", "NecromancerRobe",
    "GnollArmor", "SpiderArmor", "RatArmor", "ChaosArmor", "ElfArmor",
    "PriestArmor", "PaladinArmor", "ClericArmor", "WitchdoctorArmor",
    "AlchemistArmor", "TransmuterArmor", "PlagueDoctorArmor",
]

# short keys accepted on the CLI, mirroring the mob-viewer config
ARMOR_KEYS = {
    "cloth": "ClothArmor", "leather": "LeatherArmor", "mail": "MailArmor",
    "scale": "ScaleArmor", "plate": "PlateArmor", "gothic": "GothicArmor",
    "rogue": "RogueArmor", "warrior": "WarriorArmor", "mage": "MageArmor",
    "huntress": "HuntressArmor", "scout": "ScoutArmor", "shaman": "ShamanArmor",
    "gladiator": "GladiatorArmor", "berserk": "BerserkArmor",
    "warlock": "WarlockArmor", "battlemage": "BattleMageArmor",
    "assasin": "AssasinArmor", "freerunner": "FreeRunnerArmor",
    "sniper": "SniperArmor", "warden": "WardenArmor",
    "necromancer": "NecromancerArmor", "lich": "NecromancerArmor",
    "necromancerrobe": "NecromancerRobe", "gnoll": "GnollArmor",
    "spider": "SpiderArmor", "rat": "RatArmor", "chaos": "ChaosArmor",
    "elf": "ElfArmor", "priest": "PriestArmor", "paladin": "PaladinArmor",
    "cleric": "ClericArmor", "witchdoctor": "WitchdoctorArmor",
    "alchemist": "AlchemistArmor", "transmuter": "TransmuterArmor",
    "plaguedoctor": "PlagueDoctorArmor",
}

# armors whose helmet covers the hair (RetroHeroSpriteDef / Armor.coverHair)
COVER_HAIR = {
    "HuntressArmor", "ShamanArmor", "GuardianArmor", "GladiatorArmor",
    "WitchdoctorArmor", "BerserkArmor", "WardenArmor",
}

FRAMES = 21                       # modern indices 0..20 (idle..two-hand attack)
RETRO_COLS = 36                   # 512 / 14
DIE_FRAMES = (14, 15, 16, 17, 18) # retro empties every layer but death here


def assets_dir():
    return os.path.normpath(ASSETS)


def visual_box():
    """Read the modern visual box from the sprite descriptor."""
    with open(os.path.join(assets_dir(), MODERN, "spritesDesc", "Hero.json")) as f:
        d = json.load(f)
    ox, oy = int(d["visualOffsetX"]), int(d["visualOffsetY"])
    w, h = int(d["visualWidth"]), int(d["visualHeight"])
    return ox, oy, w, h


def body_type(hero_class, sub_class):
    if sub_class in SUBCLASS_BODY:
        return SUBCLASS_BODY[sub_class]
    if hero_class in SUBCLASS_BODY:
        return SUBCLASS_BODY[hero_class]
    if hero_class in CLASS_BODY:
        return CLASS_BODY[hero_class]
    if hero_class in FEMININE_CLASSES:
        return "woman"
    return "man"


def armor_visual(name):
    """Accept short keys ('cloth'), full names ('ClothArmor'), any casing."""
    table = {a.lower(): a for a in ARMORS}
    table.update(ARMOR_KEYS)
    visual = table.get(name.lower())
    if not visual:
        raise SystemExit(f"unknown armor '{name}' (e.g. cloth, plate, berserk, or full names like ClothArmor)")
    return visual


def source_layers(hero_class, sub_class, armors):
    """Return (src_rel, dst_rel, kind) modern->retro layer entries that exist.

    kind: 'body' (gets bare hands baked in), 'death' (reused from the
    hand-drawn retro set - the modern death sheet is too sparse for the
    retro 5-frame wisp), or 'layer' (plain crop, die frames cleared).
    """
    cd = f"{hero_class}_{sub_class}"
    bt = body_type(hero_class, sub_class)

    pairs = []
    for src, dst, kind in [
        (f"{MODERN}body/{bt}.png", f"{RETRO}body/{bt}.png", "body"),
        (f"{MODERN}head/{cd}.png", f"{RETRO}head/{cd}.png", "layer"),
        (f"{MODERN}head/hair/{cd}_HAIR.png", f"{RETRO}head/hair/{cd}_HAIR.png", "layer"),
        (f"{MODERN}head/facial_hair/{cd}_FACIAL_HAIR.png",
         f"{RETRO}head/facial_hair/{cd}_FACIAL_HAIR.png", "layer"),
        (f"{RETRO}death/{'warlock' if cd == 'MAGE_WARLOCK' else 'common'}.png",
         f"{RETRO}death/{'warlock' if cd == 'MAGE_WARLOCK' else 'common'}.png", "death"),
    ]:
        if os.path.exists(os.path.join(assets_dir(), src)):
            pairs.append((src, dst, kind))

    for armor in armors:
        armor = armor_visual(armor)
        for sub in ("", "helmet/", "collar/"):
            src = f"{MODERN}armor/{sub}{armor}.png"
            dst = f"{RETRO}armor/{sub}{armor}.png"
            if os.path.exists(os.path.join(assets_dir(), src)):
                pairs.append((src, dst, "layer"))
    return pairs


def convert_pairs(pairs, out_dir, box, into_assets, verbose=False, tag="",
                  missing_only=False):
    total = 0
    skipped = 0
    for src, dst, kind in pairs:
        if missing_only:
            if into_assets and os.path.exists(os.path.join(assets_dir(), dst)):
                skipped += 1
                continue
            if not into_assets and os.path.exists(os.path.join(out_dir, dst)):
                skipped += 1
                continue
        overlays = ()
        if kind == "body":
            bt = os.path.basename(dst)[:-4]
            hands = [f"{MODERN}body/hands/{bt}_none_{hand}.png" for hand in ("left", "right")]
            hands = [h for h in hands if os.path.exists(os.path.join(assets_dir(), h))]
            n = convert_sheet(src, dst, out_dir, box, into_assets,
                              overlays=hands, empty_frames=DIE_FRAMES)
        elif kind == "death":
            if into_assets:
                continue  # target is the original file
            d = os.path.join(out_dir, dst)
            os.makedirs(os.path.dirname(d), exist_ok=True)
            shutil.copyfile(os.path.join(assets_dir(), dst), d)
            n = 0
        else:
            # armor torso sheets get the shoulder (sleeve) layers baked in -
            # modern renders sleeves as separate shoulder overlays, retro
            # bakes them into the armor sheet. Armors without shoulder
            # files (robes) are sleeveless in-game too.
            if kind == "layer" and "/armor/" in dst and "helmet" not in dst and "collar" not in dst:
                name = os.path.basename(dst)[:-4]
                overlays = [f"{MODERN}armor/shoulders/{name}_{hand}.png" for hand in ("left", "right")]
                overlays = [o for o in overlays if os.path.exists(os.path.join(assets_dir(), o))]
            n = convert_sheet(src, dst, out_dir, box, into_assets,
                              overlays=overlays, empty_frames=DIE_FRAMES)
        total += 1
        if verbose:
            note = {"body": " (hands baked)", "death": " (copied from retro)"}.get(kind, "")
            if overlays:
                note += " (shoulders baked)"
            print(f"{tag}{src} -> {dst} ({n} frames){note}")
    if skipped:
        print(f"{tag}skipped {skipped} existing sheet(s) (--missing-only)")
    return total


def convert_sheet(src_rel, dst_rel, out_dir, box, into_assets=False,
                  overlays=(), empty_frames=()):
    """Crop one modern sheet into a retro-format sheet.

    overlays: extra modern sheets composited onto each frame before the
    crop (used to bake the bare-hand layers into the body - modern keeps
    hands in separate sheets, retro bakes them into the body).
    empty_frames: frame indices output fully transparent - retro clears
    every layer except death during the die animation, while modern
    sheets keep falling-body art there.
    """
    ox, oy, w, h = box
    src = os.path.join(assets_dir(), src_rel)
    img = Image.open(src).convert("RGBA")
    cols = img.width // 32

    ov_imgs = [Image.open(os.path.join(assets_dir(), o)).convert("RGBA") for o in overlays]

    out = Image.new("RGBA", (512, 32), (0, 0, 0, 0))
    written = 0
    for i in range(FRAMES):
        fx, fy = (i % cols) * 32, (i // cols) * 32
        if fy + 32 > img.height:
            break
        cell = img.crop((fx + ox, fy + oy, fx + ox + w, fy + oy + h))
        for j, oimg in enumerate(ov_imgs):
            ocols = oimg.width // 32
            ofx, ofy = (i % ocols) * 32, (i // ocols) * 32
            if ofy + 32 > oimg.height:
                continue
            ocell = oimg.crop((ofx + ox, ofy + oy, ofx + ox + w, ofy + oy + h))
            cell.alpha_composite(ocell)
        if i in empty_frames:
            cell = Image.new("RGBA", cell.size, (0, 0, 0, 0))
        out.alpha_composite(cell, ((i % RETRO_COLS) * w, 0))
        written += 1

    if into_assets:
        dst = os.path.join(assets_dir(), dst_rel)
    else:
        dst = os.path.join(out_dir, dst_rel)
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    out.save(dst)
    return written


def retro_stack(hero_class, sub_class, armor, base):
    """Retro z-order layer paths (base = 'hero/' or the generated dir)."""
    cd = f"{hero_class}_{sub_class}"
    bt = body_type(hero_class, sub_class)
    death = "warlock" if cd == "MAGE_WARLOCK" else "common"

    layers = [
        f"{base}body/{bt}.png",
        f"{base}armor/collar/{armor}.png" if armor else None,
        f"{base}head/{cd}.png",
        f"{base}head/hair/{cd}_HAIR.png",
        f"{base}armor/{armor}.png" if armor else None,
        f"{base}head/facial_hair/{cd}_FACIAL_HAIR.png",
        f"{base}armor/helmet/{armor}.png" if armor else None,
        f"{base}death/{death}.png",
    ]
    return [p for p in layers if os.path.exists(p)]


def compose_strip(layers, scale=6, frames=None):
    w, h = 14, 17
    frames = frames if frames is not None else list(range(19))
    strip = Image.new("RGBA", (len(frames) * (w + 1) * scale, h * scale), (40, 44, 52, 255))
    for row, i in enumerate(frames):
        for layer in layers:
            img = Image.open(layer).convert("RGBA")
            cols = img.width // w
            fx, fy = (i % cols) * w, (i // cols) * h
            if fy + h > img.height:
                continue
            cell = img.crop((fx, fy, fx + w, fy + h))
            strip.alpha_composite(cell.resize((w * scale, h * scale), Image.NEAREST),
                                  (row * (w + 1) * scale, 0))
    return strip


def cmd_convert(args):
    box = visual_box()
    armors = ARMORS if args.armor == ["all"] else args.armor
    pairs = source_layers(args.hero_class, args.sub_class, armors)
    total = convert_pairs(pairs, args.out, box, args.into_assets,
                          verbose=True, missing_only=args.missing_only)
    print(f"converted {total} layer sheet(s); visual box {box}")
    return 0


def cmd_all(args):
    box = visual_box()
    total = 0
    for cls, subs in CLASSES.items():
        for sub in subs:
            pairs = source_layers(cls, sub, ARMORS)
            if args.dry_run:
                total += len(pairs)
            else:
                total += convert_pairs(pairs, args.out, box, args.into_assets,
                                       verbose=args.verbose, tag=f"{cls}_{sub}: ",
                                       missing_only=args.missing_only)
    print(f"{'would convert' if args.dry_run else 'converted'} {total} layer sheet(s) "
          f"for {sum(len(s) for s in CLASSES.values())} class/subclass combos")
    return 0


def cmd_preview(args):
    armor = armor_visual(args.armor) if args.armor != "none" else None
    gen_base = os.path.join(args.gen_dir, RETRO)
    orig_base = os.path.join(assets_dir(), RETRO)

    gen_layers = retro_stack(args.hero_class, args.sub_class, armor, gen_base)
    orig_layers = retro_stack(args.hero_class, args.sub_class, armor, orig_base)

    # suppress hair for cover-hair helmets with a helmet file, like the game
    cd = f"{args.hero_class}_{args.sub_class}"
    if armor and armor in COVER_HAIR and \
            os.path.exists(os.path.join(assets_dir(), f"{RETRO}armor/helmet/{armor}.png")):
        hair = f"head/hair/{cd}_HAIR.png"
        gen_layers = [p for p in gen_layers if not p.endswith(hair)]
        orig_layers = [p for p in orig_layers if not p.endswith(hair)]

    gen = compose_strip(gen_layers)
    orig = compose_strip(orig_layers)

    out = Image.new("RGBA", (max(gen.width, orig.width), gen.height + orig.height + 20),
                    (25, 25, 28, 255))
    out.alpha_composite(orig, (0, 0))
    out.alpha_composite(gen, (0, orig.height + 20))

    os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
    out.save(args.out)
    print(f"wrote {args.out}")
    print(f"  top row:    original retro ({len(orig_layers)} layers)")
    print(f"  bottom row: generated from modern ({len(gen_layers)} layers)")
    if not gen_layers:
        print("  NOTE: no generated layers found - run 'convert' for this hero first")
    return 0


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)

    c = sub.add_parser("convert", help="convert one hero's layers")
    c.add_argument("--class", dest="hero_class", required=True, choices=list(CLASSES))
    c.add_argument("--subclass", dest="sub_class", default="NONE")
    c.add_argument("--armor", action="append", default=[],
                   help="armor visual name (repeatable, or 'all')")
    c.add_argument("--out", default="generated/retro")
    c.add_argument("--into-assets", action="store_true",
                   help="write into assets/hero/ (overwrites hand-drawn art!)")
    c.add_argument("--missing-only", action="store_true",
                   help="skip sheets whose retro destination already exists")
    c.set_defaults(func=cmd_convert)

    a = sub.add_parser("all", help="convert every class/subclass/armor")
    a.add_argument("--out", default="generated/retro")
    a.add_argument("--into-assets", action="store_true")
    a.add_argument("--missing-only", action="store_true",
                   help="skip sheets whose retro destination already exists")
    a.add_argument("--dry-run", action="store_true")
    a.add_argument("--verbose", action="store_true")
    a.set_defaults(func=cmd_all)

    p = sub.add_parser("preview", help="render generated vs original retro strip")
    p.add_argument("--class", dest="hero_class", required=True, choices=list(CLASSES))
    p.add_argument("--subclass", dest="sub_class", default="NONE")
    p.add_argument("--armor", default="none")
    p.add_argument("--gen-dir", dest="gen_dir", default="generated/retro",
                   help="dir containing converted sheets (from 'convert --out')")
    p.add_argument("--out", default="/tmp/retro_preview.png", help="output PNG path")
    p.set_defaults(func=cmd_preview)

    args = ap.parse_args()

    if getattr(args, "into_assets", False):
        print("WARNING: --into-assets overwrites the hand-drawn retro art in assets/hero/")
    sys.exit(args.func(args))


if __name__ == "__main__":
    main()
