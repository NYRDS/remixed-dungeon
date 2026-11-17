# Mod editor — design document

Status: round 4 (bd remixed-dungeon-its), 2026-11-16. Companion user guide: `mod_editor.md`
(what it does, how to open it). This document is the design: scope,
architecture, API contract, security model, and the coverage matrix for
every moddable aspect of the game.

## 1. Purpose and users

The mod editor lets a player turn installed mods into their own content
without a desktop toolchain:

- **Mod consumers → modders**: someone who downloaded a mod from the
  in-game browser tweaks its balance, sprites, or strings and keeps
  playing their variant.
- **Remixers**: players who want the full game as a starting point clone
  the built-in content and change whatever they like.
- **New modders**: the template scaffold gives a working two-level
  campaign with a custom mob in one click.

Non-goal: the built-in Remixed mod stays read-only on player builds
(dev platforms opt in via `canWriteMainMod`, the existing sprite-save
gate). The editor edits **user mods**, on any device the game runs on,
through the embedded web server - no desktop required.

## 2. Architecture

```
game process (android / desktop / headless)
 ├─ BaseWebServer (NanoHTTPD, embedded)
 │   ├─ /web/mod-editor/   static ES modules, no external requests
 │   └─ /api/mods/*        ModEditorEndpoints (mod-scoped API)
 ├─ game thread (GameLoop)  lua validation + destructive ops run here
 └─ mods/<name>/            the edited data (game's own resource overlay)
```

- The web app is plain ES modules served from assets; the browser is
  whatever is at hand (in-app browser via the Mods-screen button, or any
  LAN browser against the device IP).
- The legacy `/api/save-*` endpoints write into the **active** mod only;
  every `/api/mods/*` endpoint takes an explicit mod so the editor can
  target any installed mod while the game runs something else.
- Two interaction rules with the game thread:
  - **Lua compile checks** must run on the game thread
    (`pushUiTaskAndWait`); a busy thread is an error, never a silent skip.
  - **Destructive/switching ops** (delete active mod, select+restart)
    run as pushed UI tasks after the response is written; restarts exit
    the process on headless by design (`Game.doRestart`).
- The resource overlay is the editor's read model: mod file first,
  builtin asset fallback (`ModdingMode.getInputStream`). That is the same
  resolution the game uses at runtime, so previews and pickers show what
  the game will actually load.

## 3. Mod lifecycle

| Operation | Mechanism | Notes |
|---|---|---|
| Download (game UI) | unzip into `mods/<packaged root dir>` | dir names are whatever the packager used — spaces, dots, unicode |
| Create from template | POST `create` | strict name charset `[A-Za-z0-9_-]{1,64}`; scaffold: ExampleMob, Start level, 2-level campaign, strings stub; fresh `version.json` with current `rpd_version` |
| Clone builtin | POST `clone_builtin` | copies the builtin game content (whitelisted content dirs + root sheets, ~1.5k files) into a new mod; skips web apps/bootstrap assets; fresh `version.json` |
| Duplicate | POST `duplicate` | any installed dir → strict-name target |
| Edit | `/api/mods/*` read/save | see coverage matrix below |
| Validate | POST `validate` | whole-mod lint (below) |
| Export | GET `export` | zip with the mod dir at root — exactly what the install flow expects |
| Play | POST `select` | save-slot swap + restart, same flow as the mods window |
| Delete | POST `delete` | active-mod delete stashes saves back to Remixed first |

**Naming rules** (the round-3 change): a *new* mod name must match
`[A-Za-z0-9_-]{1,64}` (it becomes a file name and zip entry root), but an
*installed* mod is addressed by its real on-disk dir name — any single
path segment that resolves inside the storage root. Downloaded mods
("Remixed RPG", "D.U.N.G.E.O.N") are first-class edit targets. Traversal
(`..`, separators, absolute) is rejected and the resolved path must stay
inside the storage root; the built-in `Remixed` name keeps its read-only
treatment.

## 4. Coverage matrix — every moddable aspect

The game's moddable surface is the resource overlay plus the script
libs. This is the complete map and what the editor does for each:

| Aspect | Files | Editor |
|---|---|---|
| Mobs | `mobsDesc/*.json` | **form**: stats, strings keys, gender, walking type, aiState, loot, spriteDesc picker, scriptFile picker; unknown keys preserved |
| Mob behavior | `scripts/mobs/*.lua` | text editor (lua-compile-validated) + new-file template + snippet insert + API reference pane |
| AI states | `scripts/ai/*.lua` | text editor + template + snippets + API reference |
| Sprites | `spritesDesc/*.json` | **form**: texture, frame size, per-anim fps/loop/frames, extras; animated preview |
| Textures / sheets | `*.png` anywhere (`mobs/`, `items/`, root, …) | image editor (preview + upload replace), tree upload, "copy tileset into mod" from the level editor |
| Items | `scripts/items/*.lua` | **form** over the `desc()` table (icon picker, price, stackable…); rest of the script untouched |
| Spells | `scripts/spells/*.lua` | **form** over the `desc()` table (affinity, targeting, cost, cooldown; icon picker) + template |
| Buffs | `scripts/buffs/*.lua` | **form** over the `desc()` table (icon, name, info) + template |
| Traps | `scripts/traps/*.lua` | text editor + template |
| Level objects | `levelObjects/*.json` + `scripts/objects/*.lua` | **form**: kind, script, layer, appearance desc/sprite with clickable sheet preview; unknown keys preserved; template for both halves |
| Effects | `effects/*.json` | **form**: texture, frame size, anim fps/loop/frames; live animated preview; template; anim-without-fps falls back to text (never saves fps:0) |
| Levels | `levelsDesc/*.json` maps | **level painter**: terrain palette from `Terrain.java`, resize, entrance/exit/multiexit, mob placement, tile-var painting, `customTiles` toggle; unknown keys preserved; **WYSIWYG tiles view** (see below) |
| Campaign | `levelsDesc/Dungeon.json` (Levels + Graph) | **visual graph editor**: nodes = levels (auto-laid out by depth, draggable, layout kept in localStorage), anchor-drag wires exits + mirrored entrances, exit-index badges, edge/node Delete-key, per-level property form (kind/depth/file/music/fallbackMusic/script/treasury/feeling/nMobs/viewDistance/maxBrightness/objectsKind/size/isSafe/noFogOfWar/noBuild/isStatic + unknown keys preserved), rename id (rewrites all Graph refs), "+ level" with auto-wire, per-node "new level file" from template, Entrance select, double-click opens the level painter, live graph-issue mirror; legacy 3-tuple Graph shapes bounce to the text editor |
| Tilesets | `tilesets/*.png` | tileset picker in the level editor + copy-into-mod; image editor |
| Strings / l10n | `strings_*.json` | **pairs editor** (per-line `[key, value]`) + **per-language manager**: coverage vs the mod's/builtin english, add-language seeding |
| Sound | `sound/*.mp3/ogg/wav` | audio editor (playback + upload replace); tree upload; magic-checked saves |
| Hero customization | `hero/`, `hero_modern/` pngs | image editor / upload; clone builtin for a full set |
| Plants | `plantsDesc/*.json` | **form**: imageIndex with clickable plants.png sheet preview; unknown keys preserved; template |
| Treasury (loot tables) | `levelsDesc/*Treasury.json` | **form**: v0 and v1 (categories + item weights, categoryChance / settings.probabilities + decayFactor); non-numeric weights refuse to save; template |
| Spawn tables | `levelsDesc/Bestiary.json` | **form**: per-level-kind depth tables (mob kinds + weights) + Feelings (Chance + terrain groups); mob-kind datalist; template |
| Hero init | `hero/initHeroes.json`, `hero/initHeroesDebug.json` | **form**: per-class sections (stats, equipment slots, item/quickslot entries with inline-json extras, keyword lists), `common`/`non_expert`; unknown keys preserved |
| Actors | `scripts/actors/*.lua` | text editor + template |
| NPCs | `scripts/npc/*.lua` | text editor + template |
| Painters | `scripts/painters/*.lua` | text editor |
| Custom libs | `scripts/lib/*.lua`, `scripts/userServices/` | text editor (advanced users) + snippets + API reference |
| Tilemap descriptors | `tilemapDesc/` | validated text editor |
| UI art | `ui/*.png` | image editor / upload |
| Mod manifest | `version.json` | validated text editor (name/author/description/rpd_version) |

Everything is also reachable as a raw text/binary editor, so no aspect
is *blocked* — the forms are the sugar. Items are deliberately lua-form
(not json): the "items mountain" migration is a separate long-term
effort and the editor follows the game's data model, not the other way
round.

**WYSIWYG level rendering** (round 4): the level painter has a
`tiles (WYSIWYG)` view next to the color-coded one. For `customTiles`
levels it renders the painted `baseTileVar`/`decoTileVar`/`deco2TileVar`
layers straight from the tileset sheet (the `CustomLayerTilemap` path;
cells with no authored frame show a dimmed terrain color so the map
stays readable). Otherwise it reproduces the engine's auto-tiling:
`tilemapDesc/<tiles>.json` (fallback `tiles_x_default.json`) per-terrain
`base`+`deco` frame lists with `cell % frames.length` variation, the
water-border frame shift for `WATER_TILES..WATER`, and the
`SECRET_*_TRAP` mapping — a faithful port of `XTilemapConfiguration`.
xyz (isometric) sheets are not modeled and stay color-coded with a
notice.

Everything is also reachable as a raw text/binary editor, so no aspect
is *blocked* — the forms are the sugar. Items are deliberately lua-form
(not json): the "items mountain" migration is a separate long-term
effort and the editor follows the game's data model, not the other way
round.

## 5. Server API (`/api/mods/*`)

GET:

- `list` — installed mods + synthesized builtin row + active flag
- `files?mod=&path=[&builtin=1]` — dir listing; `builtin=1` lists the
  resource overlay (pickers)
- `read?mod=&file=[&builtin=1]` — text read, mod first / builtin
  fallback, reports `source`
- `raw?mod=&file=` — bytes with mime (images, audio); same fallback
- `export?mod=` — installable zip
- `terrain` — terrain constants + tile size for the level editor

POST:

- `create` `{name, title, author, description}` — template scaffold
- `clone_builtin` `{to, title, author}` — full-content clone
- `duplicate` `{from, to}` — from: any installed dir, to: strict name
- `delete` `{name}` — active-mod delete migrates saves + restarts
- `save` `{mod, file, content}` — validated write (below)
- `save_binary` `{mod, file, data}` — png/ogg/mp3/wav, magic-sniffed
  (`image` accepted as the legacy key name)
- `delete_file` / `rename_file` `{mod, from[, to]}`
- `select` `{name}` — save-slot swap + restart (headless: process exit)
- `validate` `{name}` — whole-mod lint

## 6. Security and robustness model

- **Mod name**: installed dirs = any single path segment inside the
  storage root; new names = strict charset. No separators, no `..`,
  no absolute names, NUL/backslash rejected.
- **File paths**: relative only (`isSafeResourcePath`), absolute-path
  containment against the storage root (`isInsideStorageRootAbs`) on
  every write target; canonicalization is avoided on purpose (dev
  rundirs keep the active mod as a symlink farm into the repo).
- **Read-only builtin**: `Remixed` writes are refused before the
  existence check ("read-only" beats "unknown") unless the platform
  opts in (`canWriteMainMod`: desktop dev, headless).
- **Binary saves**: extension allowlist + magic-byte sniff per format
  (PNG `89 50 4E 47`, OGG `OggS`, WAV `RIFF`, MP3 ID3/MPEG sync).
- **Zip export**: top-level dir = mod name (what `UnzipTask` expects),
  5000-file budget.
- **Clone budgets**: depth ≤ 16, ≤ 5000 files, ≤ 128 MB; failure cleans
  the partial target dir. The walk uses accept-all listings only —
  a rejecting `FilenameFilter` makes `ModdingMode.listResources`
  *descend into* rejected dirs, which would walk the rundir (and the
  clone itself) on desktop/headless.
- **Lua**: compile checks on the game thread; busy thread ⇒ refused
  save, never an unchecked success.

## 7. Validation rules (`validate`)

Errors:

- any `.json` that neither parses strictly nor survives the hjson
  lenient path the loaders use
- `strings_*.json` line not a `["key","value"]` pair
- any `.lua` that does not compile (engine luaj, on the game thread)
- mob chain: **explicit** `spriteDesc` pointing at nothing; resolved
  spriteDesc's `texture` missing; `scriptFile` missing
- campaign: level `file` missing; `map.length != width*height`;
  `PredesignedLevel`/`RandomLevel` without `file`
- treasury: non-numeric item weight or `categoryChance`; v1 without
  `categories`/`settings` objects; non-numeric `decayFactor` or category
  probability; unknown version (the numeric-weight rules mirror the live
  Crashlytics `getDouble`/NaN family)
- bestiary: non-object section/depth table; non-numeric mob weight

Warnings:

- mob silently wearing a builtin sprite (modder's custom mob looks like
  a rat — the #1 new-modder confusion)
- mob with **no** authored spriteDesc and no default file (builtin
  mobs like Yog ship this way; engine uses its fallback sprite)
- entrance/exit/multiexit outside the map or buried in wall/chasm
- level files not referenced from `Dungeon.json` (unreachable)
- treasury: category with no `settings.probabilities` entry or with
  probability 0 (never rolls); every item weight 0 (dead category); v0
  category without `categoryChance` (misaligns the roll lists)
- bestiary: unknown mob kind (the game falls back to a rat)

## 8. Testing

- `tests/http_api/test_mod_editor.py` — E2E against the headless jar:
  create → edit → guards (traversal, name traversal, main-mod,
  magic-mismatch binaries) → downloaded-style dir names (space+dot) →
  audio binaries → clone_builtin → validate → export → select+restart →
  in-game Start-room proof → delete. Runs in CI (python-tests workflow).
- Platform compile gates (android fdroid flavor) — the endpoints class
  is main-source and builds everywhere; API-26-only `org.json` methods
  are avoided (`keys()` iterator, not `keySet()`).
- **New editor JS requires one real browser pass** before "done" —
  http tests never load the web modules; a readonly-IDL-property crash
  once shipped a dead-on-arrival editor that code review missed.

## 9. Roadmap (not yet in scope)

- Mod publishing (upload to ru.nyrds.net) — export zip covers sharing
  for now; needs server-side upload/auth design
- Items as json (the "items mountain") — editor will follow the data
  model when that lands
- WYSIWYG for xyz (isometric) tilesets — standard sheets render as real
  tiles since round 4; the 3-layer xyz layout is not modeled

Shipped in round 4 (2026-11-16, from this list): WYSIWYG level
rendering, Treasury/initHeroes/plantsDesc/Bestiary forms, per-language
strings management, lua authoring help (snippets + API reference pane).
