# AI Sprite Creation Guide (Remixed Dungeon)

Instructions for AI agents authoring new mob sprites in this repository.
The companion tool is `tools/sprite_tool.py` — use it for every check it
supports; it exits non-zero on hard errors so it can gate your work.

## 1. The asset chain (no Java required)

A new mob is three files (plus optional Lua) — `MobFactory` scans
`mobsDesc/*.json` at startup and auto-registers every descriptor as a
data-driven `CustomMob`:

```
RemixedDungeon/src/main/assets/
├── mobsDesc/<Kind>.json          # mob stats, name, links
├── spritesDesc/<Kind>.json       # texture + animation frame sequences
├── mobs/<kind>.png               # sprite sheet (or repo root for legacy sheets)
└── scripts/mobs/<Kind>.lua       # optional behavior (scriptFile)
```

`mobsDesc/<Kind>.json`:

```json
{
  "defenseSkill": 5, "attackSkill": 12, "exp": 3, "maxLvl": 1,
  "dmgMin": 2, "dmgMax": 6, "dr": 2, "ht": 15,
  "loot": {"kind": "RatSkull"}, "lootChance": 1,
  "name": "Mud Slime", "name_objective": "Mud Slime",
  "description": "A damp green blob.",
  "gender": "nonthropic",
  "spriteDesc": "spritesDesc/MudSlime.json",
  "walkingType": "NORMAL",
  "scriptFile": "scripts/mobs/MudSlime"
}
```

Spawn it from Lua with `RPD.spawnMob("MudSlime", cell)` (see
`scripts/lib/commonClasses.lua`), or add it to a level's spawn tables.
Behavior callbacks (`stats`, `act`, `attackProc`, ...) come from
`require "scripts/lib/mob"` — real examples: `scripts/mobs/RemixedFetidRat.lua`,
`scripts/mobs/ScriptedThief.lua`.

## 2. Sheet format — ground rules

Frames are laid out in fixed-size cells, numbered **row-major**. From
`TextureFilm.java`:

```java
cols = texWidth / frameWidth;  rows = texHeight / frameHeight;  // int division
// frame index i -> row i/cols, col i%cols
```

- Typical cell is 16x16; other sizes exist (kobold 12x15, rat 16x15). The
  cell size lives in the spritesDesc `width`/`height`, not in the PNG.
- `rat.png` is 256x32 with 16x15 cells: 2 rows of cells, second row is a
  palette-swapped variant addressed by frame indices 16+. `texHeight` not
  dividing evenly is legal (rat wastes 2px); keep sheets 256px wide by
  convention.
- A referenced frame index >= rows*cols crashes at runtime
  (`TextureFilm.get()` returns null). The validator checks this.

### spritesDesc schema

```json
{
  "texture": "mobs/mudslime.png",
  "width": 16, "height": 16,
  "idle":   {"fps": 4,  "looped": true,  "frames": [0,1]},
  "run":    {"fps": 10, "looped": true,  "frames": [2,3,4,5]},
  "attack": {"fps": 10, "looped": false, "frames": [6,7,8]},
  "die":    {"fps": 10, "looped": false, "frames": [9,10,11]},
  "bloodColor": "0xff4a9020",
  "zapEffect": "Shadow"
}
```

- Frame lists may repeat and reorder indices; reuse frames across
  animations where it reads well (the Bee's run reuses its idle frames).
- Optional: `bloodColor`, `zapEffect`, `particleEmitters` — see
  `docs/advanced_sprite_examples.md` for emitters and physics extras.

## 3. Workflow (mandatory order)

1. **Prefer derivation over invention**, in this order:
   a. Recolor of an existing sheet (`recolor` subcommand) — e.g. ice/fire
      variants.
   b. Patching an existing sheet (swap a head, add an accessory).
   c. Original art — last resort, follow the craft rules below.
2. Draw/assemble the sheet (see `assemble`, or generate with a script —
   worked example in section 5).
3. `validate` the sheet + spritesDesc. Fix every error; for each warning,
   either fix the art or confirm in the preview that it is intentional.
4. `preview` and **actually view the rendered PNG** (Read tool) — check
   silhouette readability, clipping, ground line, animation flow.
5. Write `mobsDesc/<Kind>.json`, then `check-mob <Kind>` to verify the
   full chain resolves (descriptor -> spriteDesc -> texture -> script).
6. Optional in-browser check: `mob-viewer/` plays animations from the
   real descriptors (`python3 -m http.server 8080` then open
   `/mob-viewer/index.html`).

## 4. Hard rules (validator-enforced)

- All four animations present (`idle`, `run`, `attack`, `die`) with
  positive `fps` and explicit `looped`.
- No referenced frame may be fully transparent or out of capacity.
- No clipped art: a frame cut by its cell boundary leaves a detectable
  bar of opaque pixels at the edge (top bar, or side bar on frames tall
  enough to be a body, not a puddle). Warnings — confirm or fix.
- Keep creatures bottom-anchored: feet on the cell's last content row,
  consistent across idle/run (death frames flatten and are exempt).
- Keep sprites 1px inside the cell on idle frames; only attack lunges,
  jump peaks and death puddles should reach the edges.

## 5. Craft rules for original art

- Silhouette first: the mob must be identifiable in one color. Then
  outline (darker shade), then highlight. Keep the whole palette under
  ~16 colors; reuse existing mobs' palettes when possible.
- Frame budget: idle 2-4, run 4-6, attack 3-5, die 3-4. Idle is subtle
  (breathe/bob at 2-4 fps), run reads as squash/stretch/hop at 10-15
  fps, attack is a fast anticipation-strike-recover, die collapses
  while fading alpha 255 -> 80.
- Eyes are 1-2px dark dots with a white glint; put them high on the body.
- When generating programmatically, clamp every transform so content
  provably fits its cell (the worked example below), because the engine
  silently crops at cell boundaries — this exact bug produced a
  flat-topped slime once; `validate` now catches it.

## 6. Tool reference

```bash
T=tools/sprite_tool.py
A=RemixedDungeon/src/main/assets

python3 $T validate $A/rat.png $A/spritesDesc/BlackRat.json
python3 $T preview $A/mobs/bee.png $A/spritesDesc/Bee.json -o /tmp/bee.png --scale 8
python3 $T assemble /tmp/f0.png,/tmp/f1.png,/tmp/f2.png -o /tmp/sheet.png --frame-width 16 --frame-height 16
python3 $T recolor $A/mobs/kobold.png -o /tmp/kobold_ice.png --hue 160 --sat 1.3
python3 $T check-mob BlackRat          # validates mobsDesc -> spriteDesc -> texture -> script
```

`validate` and `check-mob` exit 1 on errors — gate your work on them.

## 7. Worked example — Mud Slime (original art, programmatically drawn)

Design a 16x16 base pose as a character grid, derive all frames with a
**clamped** transform, then let the validator verify:

```python
def transform(img, sx=1.0, sy=1.0, dx=0, dy=0, alpha=255):
    nw, nh = max(1, round(16*sx)), max(1, round(16*sy))
    out = img.resize((nw, nh), Image.NEAREST)
    x = max(0, min(16 - nw, dx + (16 - nw)//2))
    y = max(0, min(16 - nh, dy + (16 - nh)))          # bottom-anchored, clamped
    canvas = Image.new('RGBA', (16,16), (0,0,0,0))
    canvas.alpha_composite(out, (x, y))
    if alpha < 255:
        canvas.putalpha(canvas.getchannel('A').point(lambda v: v*alpha//255))
    return canvas
```

Frame layout used: `[0,1]` idle (base, 90% squash), `[2,3,4,5]` run
(squash, stretch, hop dy=-2, land), `[6,7,8]` attack (mouth overlay,
lunge dx=1, recover), `[9,10,11]` die (flatten to 55/30/14% height,
alpha 220/150/80). Assemble into a 256x16 strip, then wire:

```json
// spritesDesc/MudSlime.json
{"texture": "mobs/mudslime.png", "width": 16, "height": 16,
 "idle":   {"fps": 4,  "looped": true,  "frames": [0,1]},
 "run":    {"fps": 10, "looped": true,  "frames": [2,3,4,5]},
 "attack": {"fps": 10, "looped": false, "frames": [6,7,8]},
 "die":    {"fps": 10, "looped": false, "frames": [9,10,11]},
 "bloodColor": "0xff4a9020"}
```

Run `check-mob MudSlime`, then spawn from Lua: `RPD.spawnMob("MudSlime", cell)`.
