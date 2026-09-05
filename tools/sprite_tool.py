#!/usr/bin/env python3
"""
sprite_tool.py - authoring/validation tools for Remixed Dungeon mob sprite sheets.

Built for AI-driven sprite creation. See docs/ai_sprite_creation.md for the
authoring guide. Requires: Python 3.8+, Pillow.

Ground truth (TextureFilm.java): frames are numbered ROW-MAJOR,
  col = index % cols, row = index / cols,
  cols = texWidth / frameWidth, rows = texHeight / frameHeight (int division).

Subcommands:
  validate   check a sheet + spritesDesc json against the hard rules
  preview    render an enlarged annotated contact sheet PNG
  assemble   build a sheet PNG from individual frame PNGs
  recolor    hue/saturation/brightness shift a sheet (palette-swap derivatives)
  check-mob  resolve and validate the full mobsDesc -> spritesDesc -> texture chain

Exit code 1 on any validation error, so it can gate commits.
"""
import argparse
import colorsys
import json
import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

SCALE = 8
ANIMS = ("idle", "run", "attack", "die")
ANIM_COLORS = {"idle": (80, 200, 120), "run": (80, 160, 255),
               "attack": (255, 120, 80), "die": (200, 200, 200)}


class Report:
    def __init__(self):
        self.errors = []
        self.warnings = []

    def error(self, msg):
        self.errors.append(msg)

    def warn(self, msg):
        self.warnings.append(msg)

    def finish(self):
        for w in self.warnings:
            print("WARN: " + w)
        for e in self.errors:
            print("ERROR: " + e)
        if self.errors:
            print("FAILED: %d error(s), %d warning(s)" % (len(self.errors), len(self.warnings)))
            return 1
        print("OK (%d warning(s))" % len(self.warnings))
        return 0


def load_desc(path):
    with open(path) as f:
        return json.load(f)


def grid(desc, sheet_w, sheet_h):
    """Frame grid per TextureFilm.java integer math."""
    fw, fh = desc["width"], desc["height"]
    cols = sheet_w // fw
    rows = sheet_h // fh
    return fw, fh, cols, rows


def cell_box(index, fw, fh, cols):
    row, col = index // cols, index % cols
    return col * fw, row * fh


def cell_content(sheet, index, fw, fh, cols):
    """Return (bbox, opaque_px) of one cell's non-transparent content."""
    x0, y0 = cell_box(index, fw, fh, cols)
    cell = sheet.crop((x0, y0, x0 + fw, y0 + fh))
    alpha = cell.getchannel("A")
    data = list(alpha.getdata())
    return alpha.getbbox(), sum(1 for p in data if p > 0)


def referenced_frames(desc):
    seen = {}
    for anim in ANIMS:
        if anim not in desc:
            continue
        for i in desc[anim].get("frames", []):
            seen.setdefault(i, []).append(anim)
    return seen


# ---------------------------------------------------------------- validate

def cmd_validate(args):
    rep = Report()
    sheet = Image.open(args.sheet).convert("RGBA")
    desc = load_desc(args.desc)
    fw, fh, cols, rows = grid(desc, sheet.width, sheet.height)

    print("sheet %dx%d, frame %dx%d -> %d cols x %d rows, capacity %d"
          % (sheet.width, sheet.height, fw, fh, cols, rows, cols * rows))

    if sheet.width % fw != 0:
        rep.warn("sheet width %d not a multiple of frame width %d (right strip unreachable)" % (sheet.width, fw))
    if sheet.height % fh != 0:
        rep.warn("sheet height %d not a multiple of frame height %d (bottom strip unreachable, rat.png does this too)" % (sheet.height, fh))

    for anim in ANIMS:
        if anim not in desc:
            rep.warn("missing animation '%s'" % anim)
            continue
        a = desc[anim]
        if not a.get("frames"):
            rep.error("animation '%s' has empty frame list" % anim)
        if not a.get("fps") or a["fps"] <= 0:
            rep.error("animation '%s' has invalid fps" % anim)
        if "looped" not in a:
            rep.error("animation '%s' missing 'looped'" % anim)

    used = referenced_frames(desc)
    if not used:
        rep.error("no frames referenced at all")
        return rep.finish()

    cap = cols * rows
    alpha = sheet.getchannel("A")
    ground_lines = {}
    for idx in sorted(used):
        if idx >= cap:
            rep.error("frame %d (in %s) outside capacity %d - TextureFilm.get() returns null and will crash"
                      % (idx, ",".join(used[idx]), cap))
            continue
        bbox, opaque = cell_content(sheet, idx, fw, fh, cols)
        if bbox is None or opaque == 0:
            msg = "frame %d (in %s) is fully transparent" % (idx, ",".join(used[idx]))
            # hero layer sheets legitimately have empty frames (body/armor
            # vanish during the death anim; the death layer takes over)
            if args.allow_empty:
                rep.warn(msg + " (allowed by --allow-empty)")
            else:
                rep.error(msg)
            continue
        cx0, cy0 = cell_box(idx, fw, fh, cols)
        x0, y0, x1, y1 = bbox  # content extent inside the cell
        content_h = y1 - y0
        # truncation signatures: a clipped frame leaves a solid bar of opaque
        # pixels along the cell edge; intentional wide poses (lunge, death
        # puddle) leave only sparse pixels there
        top_bar = sum(1 for x in range(x0, x1) if alpha.getpixel((cx0 + x, cy0)) > 0)
        if y0 == 0 and top_bar >= 3:
            rep.warn("frame %d (in %s): flat-topped, likely clipped by its cell"
                     % (idx, ",".join(used[idx])))
        left_bar = sum(1 for y in range(y0, y1) if alpha.getpixel((cx0, cy0 + y)) > 0)
        right_bar = sum(1 for y in range(y0, y1) if alpha.getpixel((cx0 + fw - 1, cy0 + y)) > 0)
        if content_h >= 6 and (left_bar >= content_h * 0.6 or right_bar >= content_h * 0.6):
            rep.warn("frame %d (in %s): solid bar at cell edge, art likely cut off"
                     % (idx, ",".join(used[idx])))
        anims = set(used[idx])
        if anims & {"idle", "run"}:
            ground_lines.setdefault(y1, []).append(idx)
        if opaque < 8:
            rep.warn("frame %d nearly empty (%d opaque px)" % (idx, opaque))

    if len(ground_lines) > 1:
        for gl, idxs in sorted(ground_lines.items()):
            print("info: ground line bottom row %s in frames %s (varies legitimately for hop cycles)" % (gl, idxs))

    unused = [i for i in range(cap) if i not in used]
    if unused:
        print("info: %d unused cell(s): %s" % (len(unused), unused))

    return rep.finish()


# ---------------------------------------------------------------- preview

def cmd_preview(args):
    sheet = Image.open(args.sheet).convert("RGBA")
    desc = load_desc(args.desc)
    fw, fh, cols, rows = grid(desc, sheet.width, sheet.height)
    scale = args.scale

    used = referenced_frames(desc)
    out_w = cols * fw * scale
    out_h = rows * fh * scale + 18 * rows + 4
    img = Image.new("RGB", (out_w, out_h), (30, 30, 34))
    draw = ImageDraw.Draw(img)

    # checkerboard so transparency is visible
    c = 12
    for cy in range(0, rows * fh * scale, c):
        for cx in range(0, cols * fw * scale, c):
            if (cx // c + cy // c) % 2 == 0:
                draw.rectangle([cx, cy, cx + c - 1, cy + c - 1], fill=(44, 44, 50))

    try:
        font = ImageFont.load_default(scale * 2)
    except TypeError:
        font = ImageFont.load_default()

    for idx in range(cols * rows):
        x0, y0 = cell_box(idx, fw, fh, cols)
        dx, dy = x0 * scale, y0 * scale + (y0 // fh) * 18
        cell = sheet.crop((x0, y0, x0 + fw, y0 + fh)).resize((fw * scale, fh * scale), Image.NEAREST)
        img.paste(cell, (dx, dy), cell)
        draw.rectangle([dx, dy, dx + fw * scale - 1, dy + fh * scale - 1],
                       outline=(70, 70, 78))
        anims = ",".join(used.get(idx, []))
        color = ANIM_COLORS.get(anims.split(",")[0], (160, 160, 160)) if anims else (90, 90, 96)
        draw.text((dx + 2, dy + fh * scale + 2), "%d %s" % (idx, anims), fill=color, font=font)

    img.save(args.output)
    print("wrote %s (%dx%d)" % (args.output, img.width, img.height))
    return 0


# ---------------------------------------------------------------- assemble

def cmd_assemble(args):
    rep = Report()
    frames = [Path(p) for p in args.frames.split(",")]
    fw, fh = args.frame_width, args.frame_height
    cols = args.cols if args.cols else 256 // fw
    rows = math.ceil(len(frames) / cols)
    sheet = Image.new("RGBA", (cols * fw, max(rows, 1) * fh), (0, 0, 0, 0))
    for i, p in enumerate(frames):
        fr = Image.open(p).convert("RGBA")
        if fr.size != (fw, fh):
            rep.error("%s is %dx%d, expected %dx%d" % (p, fr.width, fr.height, fw, fh))
            continue
        x0, y0 = cell_box(i, fw, fh, cols)
        sheet.paste(fr, (x0, y0))
    if not rep.errors:
        sheet.save(args.output)
        print("wrote %s (%dx%d, %d frames, %d cols)" % (args.output, sheet.width, sheet.height, len(frames), cols))
    return rep.finish()


# ---------------------------------------------------------------- recolor

def cmd_recolor(args):
    img = Image.open(args.sheet).convert("RGBA")
    rgba = img.split()
    rgb = Image.merge("RGB", rgba[:3]).convert("HSV")
    h, s, v = rgb.split()

    h = h.point(lambda x: (x + round(args.hue * 255 / 360)) % 256)
    if args.sat != 1.0:
        s = s.point(lambda x: max(0, min(255, round(x * args.sat))))
    if args.value != 1.0:
        v = v.point(lambda x: max(0, min(255, round(x * args.value))))

    out = Image.merge("HSV", (h, s, v)).convert("RGB").convert("RGBA")
    out.putalpha(rgba[3])
    out.save(args.output)
    print("wrote %s (hue %+d deg, sat x%.2f, value x%.2f)" % (args.output, args.hue, args.sat, args.value))
    return 0


# ---------------------------------------------------------------- check-mob

def cmd_check_mob(args):
    """Resolve the full chain: mobsDesc/<Kind>.json -> spriteDesc -> texture."""
    rep = Report()
    assets = Path(args.assets)
    mob_desc_path = assets / "mobsDesc" / (args.kind + ".json")
    if not mob_desc_path.exists():
        rep.error("missing %s" % mob_desc_path)
        return rep.finish()

    mob = load_desc(mob_desc_path)
    print("mob '%s': %s" % (args.kind, mob.get("name", "?")))

    sprite_rel = mob.get("spriteDesc")
    if not sprite_rel:
        rep.error("mobsDesc has no spriteDesc")
        return rep.finish()
    sprite_path = assets / sprite_rel
    if not sprite_path.exists():
        rep.error("missing spriteDesc file %s" % sprite_path)
        return rep.finish()

    sdesc = load_desc(sprite_path)
    tex_rel = sdesc.get("texture")
    if not tex_rel:
        rep.error("spriteDesc has no texture")
        return rep.finish()
    tex_path = assets / tex_rel
    if not tex_path.exists():
        rep.error("missing texture %s" % tex_path)
        return rep.finish()

    print("chain ok: %s -> %s -> %s" % (mob_desc_path.name, sprite_rel, tex_rel))

    script = mob.get("scriptFile")
    if script:
        if not (assets / (script + ".lua")).exists():
            rep.error("scriptFile %s.lua not found in assets" % script)
        else:
            print("script: %s.lua" % script)

    if args.lua_assets and script and (Path(args.lua_assets) / (script.rsplit("/", 1)[-1] + ".lua")).exists():
        print("note: same-named script also exists in %s" % args.lua_assets)

    return cmd_validate(argparse.Namespace(sheet=str(tex_path), desc=str(sprite_path)))


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)

    v = sub.add_parser("validate", help="validate sheet + spritesDesc")
    v.add_argument("sheet")
    v.add_argument("desc")
    v.add_argument("--allow-empty", action="store_true",
                   help="transparent frames are warnings, not errors (hero layer sheets)")
    v.set_defaults(func=cmd_validate)

    p = sub.add_parser("preview", help="render annotated contact sheet")
    p.add_argument("sheet")
    p.add_argument("desc")
    p.add_argument("-o", "--output", default="preview.png")
    p.add_argument("--scale", type=int, default=SCALE)
    p.set_defaults(func=cmd_preview)

    a = sub.add_parser("assemble", help="build sheet from frame PNGs")
    a.add_argument("frames", help="comma-separated frame PNGs, in index order")
    a.add_argument("-o", "--output", required=True)
    a.add_argument("--frame-width", type=int, default=16)
    a.add_argument("--frame-height", type=int, default=16)
    a.add_argument("--cols", type=int, default=0, help="cells per row (default 256/fw)")
    a.set_defaults(func=cmd_assemble)

    r = sub.add_parser("recolor", help="palette-swap a sheet")
    r.add_argument("sheet")
    r.add_argument("-o", "--output", required=True)
    r.add_argument("--hue", type=float, default=0, help="degrees 0-360")
    r.add_argument("--sat", type=float, default=1.0)
    r.add_argument("--value", type=float, default=1.0)
    r.set_defaults(func=cmd_recolor)

    m = sub.add_parser("check-mob", help="validate full mob descriptor chain")
    m.add_argument("kind", help="mob kind (mobsDesc/<Kind>.json)")
    m.add_argument("--assets", default="RemixedDungeon/src/main/assets")
    m.add_argument("--lua-assets", default="scripts")
    m.set_defaults(func=cmd_check_mob)

    args = ap.parse_args()
    sys.exit(args.func(args))


if __name__ == "__main__":
    main()
