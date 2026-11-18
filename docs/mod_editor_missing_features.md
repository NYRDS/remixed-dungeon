# Mod editor: live test + missing-features report (round 5, 2026-11-17)

Scope: full review + live test of the mod editor as shipped in c71451c7f (round 4), against
HEAD master. Live test = `tests/http_api/test_mod_editor.py` E2E on the freshly built headless
jar (verified byte-identical to the working tree) + an in-app-browser pass over every round-4
editor with a seeded QA mod (`QaRound5`). Gap inventory cross-checked against
`docs/mod_editor_design.md` and the engine's actual content systems.

**Verdict: no P1 regressions. All round-4 features work live.** The gaps below are missing
coverage, not broken behavior — the editor's own fallback discipline (bad shape → text editor)
proved itself twice during testing (see live-test notes).

---

## 1. Live test results (all PASS)

| Feature | How tested | Result |
|---|---|---|
| E2E suite (round-4 additions incl. rename/files/read/terrain/validate rules) | `RPD_HEADLESS_JAR=… test_mod_editor.py` | ALL PASS (exit 0) |
| Treasury editor v1 | form render, decayFactor 2→3, Pasty weight 1→7, Save, disk verify | PASS; probabilities/categories/unknown keys preserved |
| Bestiary editor | per-level depth tables + Feelings groups, Gnoll 2→3.5, Save | PASS; Feelings groups preserved; mob datalist populated |
| Init-heroes editor | sections common/WARRIOR, 4 equip slots, entry lists, hp 22→25 | PASS; `allies` (non-form key) survived |
| Plants editor | imageIndex 3→7, plants.png sheet canvas (144×16 = 9×16px frames) | PASS |
| Strings manager | pair edit round-trip, coverage column, `+ language` (prompt shim) → strings_de.json seeded with 3 empty pairs | PASS |
| Lua help (text editor path) | 9 snippet buttons, insert into textarea works, API-ref pane | PASS |
| WYSIWYG customTiles path | QaLevel (customTiles:true): colors view 20 colors → tiles view 107 colors | PASS |
| WYSIWYG autotile path | plain level, default sheet: 17 colors → 98 colors via builtin `tilemapDesc/tiles_x_default.json` fallback, no notice | PASS |
| xyz-sheet bounce | template Start.json (tiles0_x.png): stays color-coded with notice | PASS (by design) |
| validate | whole-mod run caught all seeded issues: unreachable levels, UNIQUE probability missing, builtin sprite reuse | PASS |
| Item editor (desc-table form) | seeded lua parsed into 10 form fields (name/image/price/stackable…) | PASS |

Live-test observations (not bugs, but worth recording):

- **Treasury save injects `settings.probabilities[<cat>] = 0` for categories that have no
  probabilities entry** (treasury-editor.js:304-306, deliberate sync). The validate warning
  "category X has no settings.probabilities entry" therefore becomes "zero probability"
  (same warning class). Fine, but the form could default the input to a non-zero value or
  show the warning inline.
- **Bestiary/Feelings fallback is load-bearing**: a scalar `{"CHASM": 0.2}` feelings map
  (invalid per engine `Bestiary.java:84` which reads `optJSONObject(feeling)`) correctly
  bounces to the text editor. Good gate.
- **Item/spell/buff form editors have no lua-help pane** — snippets + API reference only
  appear in the plain-text editor path. A modder living in the item form never sees the API
  reference. Suggest: attach the collapsible help pane to desc-table editors too.
- **Stale hint**: plants-editor.js:64 says a `scripts/plants/<Kind>.lua` "new-file template"
  exists — templates.js has no `scripts/plants/` key, so a new file is created empty.
- **In-app browser automation traps this round**: a pending dirty-confirm dialog silently
  swallows subsequent `mod-editor:open-file` dispatches (IAB auto-dismisses confirm →
  `confirmDirty()` false); shim `window.confirm` in every test cell. Also: a delayed
  promise-resolve after dispatching open-file loses the evaluate response — use sync
  evaluates + server-side (curl) verification.

---

## 2. Missing features — content types with no editor

Every structured editor mutates in place and preserves unknown keys, so none of these cause
data loss — they are reachability/UX gaps only (generic text editor always works).

| Gap | Priority | Evidence |
|---|---|---|
| **Alchemy recipes** `scripts/alchemy_recipes.json` (`{recipes:[{input:[{name,count}],outputs:[…]}]}`) — moddable, loaded via mod overlay, no form, no template, absent from design-doc matrix | **P2** | AlchemyRecipes.java:52 |
| **Quests** — quest state machines via `scripts/lib/quest.lua`; documented content system (docs/modding.md §10) but no quest template, no snippet, no scaffolding | **P2** | scripts/lib/quest.lua |
| **Hero sprite descriptor** `hero/spritesDesc/Hero.json` + hero layer pngs — dispatch prefix `spritesdesc/` does not match `hero/spritesDesc/` → plain json text, no sprite form/preview | **P3** (live-confirmed) | app.js:477, RetroHeroSpriteDef.java:30 |
| **`spritesDesc/items.json` mis-modeled**: per-item map `{kind:{file,index,fliesStraight,…}}` opens in the MobSpriteDef form (live: "Sprite: items", 6 inputs, texture empty) — saving would inject width/height junk keys; no shape gate unlike effect/treasury/bestiary | **P3** (live-confirmed) | ItemSpritesDescription.java:12 |
| `scripts/plants/*.lua` — no template despite in-code hint (see above) | P3 | templates.js:3-228 |
| Custom painters (`scripts/painters/`), services (`scripts/services/`), stats/shields, startup quirks (`scripts/startup/quirks.lua`) — text only, no templates/snippets, not in matrix | P3 | LuaEngine.java:186, Scene.java:21 |
| **Custom fonts** (`fonts/`) — `save_binary` allowlist is png/ogg/mp3/wav only; a font cannot be uploaded/replaced through any editor path | P3 | ModEditorEndpoints.java:360-364 |
| `.jpg` previewed but not uploadable (same allowlist) | P4 | text-editors.js:444 |

## 3. Missing features — engine keys the forms don't expose

| Gap | Priority | Evidence |
|---|---|---|
| **Mob json**: form covers 12 stats + 3 checks + loot.kind only. Engine also reads `isBoss`, `npc`, `fraction`, `immunities`/`resistances` (arrays), `battleMusic`/`battleMusicFallback`, `attackRange`, `spriteLayer`, `isHumanoid`, `movable`, `immortal`, `pacified`, `carcassChance`, `var`, `hasBodyParts`, `loot.category` (treasury roll), `undead`/`demon`, `level` depth-scaling — all only reachable via raw json | **P2** | Mob.java:954-1043, :897-935 |
| **Level editor**: no tools for per-cell `objects[]` (PredesignedLevel.java:89-97) or per-cell `items[]` (:181-196), no `compassTarget` (:119-127) — spawn placement is mobs-only today | **P2** |
| ~~Campaign entries~~ **CLOSED 2026-11-17 (round 6)**: `levelsDesc/Dungeon.json` now opens a visual campaign graph editor — nodes auto-laid out by depth (draggable, layout in localStorage), anchor-drag wires exits + mirrored entrances with exit-index badges, Delete-key removes edges/nodes, full per-level property form (all keys from §7a incl. nMobs/noBuild/feeling/isStatic/viewDistance/objectsKind/fallbackMusic), rename id rewrites every Graph reference, "+ level" auto-wires from the selection, per-node level-file creation from template, Entrance select, double-click opens the level painter, live graph-issue mirror + new server-side graph validation (dangling refs = error, one-way edges deliberately NOT flagged — calibrated on builtin + published mods) | closed | campaign-editor.js, ModEditorEndpoints.validateCampaignGraph |
| InitHeroes: form omits `allies` (starting pets), `left_hand` slot, `lvl`, `sl`, `startingSp`, `resistances`; conversely the form's `isSpellUser` checkbox is not consumed by the initHeroes loader (computed at runtime elsewhere) | P3 | HeroClass.java:120-197, Belongings.java:527-581 |
| Sprite editor texture datalist only lists ``/`mobs/`/`items/` — textures in other mod dirs (e.g. `plants/`, `fonts/`, custom dirs) not pickable | P3 | sprite-editor.js:94 |

## 4. Missing features — file ops and UX

| Gap | Priority | Evidence |
|---|---|---|
| **No undo/redo anywhere** — level painter and structured forms mutate state irreversibly (only textarea native undo) | **P2** |
| No search across mod files (grep/find UI) | P3 |
| No autosave; dirty state = per-editor flag + beforeunload confirm only | P3 |
| Validation output is plain text (capped at 30 entries) — no click-to-navigate to the offending file | P3 | app.js:421,435 |
| No file copy/duplicate-file UI; no new-directory affordance (dirs appear implicitly on save) | P3 | file-tree.js:80-103 |
| Upload is single-file per picker; no directory upload | P3 | upload.js:21-50 |
| 30 s client abort on every request — `clone_builtin` measured 0.12 s for 1557 files locally, so this bites only on slow storage; demoted to P4 | P4 | api.js:11 |
| ~~`onSaved` callbacks are no-ops~~ **FIXED 2026-11-17**: they were worse than no-ops — every editor's `onSaved: () => refreshTree()` threw `ReferenceError` AFTER a successful save (openFile is module-scoped, refreshTree lives in openWorkspace), so the status line showed an error for a save that actually landed and the tree never refreshed. Now a module-level `currentRefreshTree` hook registered by the workspace; also gives structured editors' text-fallbacks dirty tracking (`adoptTextFallback`) | fixed | app.js |
| Strings manager: no language delete (tree delete works), no side-by-side two-language view, no "insert missing key" action | P4 | text-editors.js:200-285 |
| `raw?builtin=1` param supported but never sent by the UI (harmless; handler falls back anyway) | P4 | ModEditorEndpoints.java:302 |
| level-editor meta key list includes `decoName` then skips it (dead entry) | P4 | level-editor.js:730-731 |

## 5. Design-doc staleness (docs/mod_editor_design.md)

- §matrix does not claim form completeness, but nothing in it (or in code) mentions alchemy
  recipes, quests, painters, services, startup scripts — add rows so the coverage matrix is
  the real inventory.
- :88 "Textures/sheets *.png anywhere — image editor" — accurate for png; jpg is
  preview-only (server allowlist).
- :108 "Custom libs … + snippets" — snippet buttons render only for the 9 SNIPPET_KEYS dirs
  (text-editors.js:76-77); scripts/lib/ and userServices get the API-ref pane only.
- :113-136 "everything is also reachable as a raw editor, so no aspect is blocked" — false
  for binaries outside png/ogg/mp3/wav (fonts).
- Roadmap §9 (publishing endpoint, items-as-json) still accurate — neither exists.
- In-code claim (plants template hint) — see §1.

## 6. Suggested order of attack (next rounds)

1. **Round 5a (small, high value):** alchemy-recipes form + template; quests template +
   snippet; plants lua template (fix the stale hint); lua-help pane on desc-table editors;
   validation entries click-to-navigate; clone_builtin client timeout raise.
   *(shipped 2026-11-17 in 8407dfe39 except alchemy/quests — see 5gh)*
2. **Round 5b (forms):** mob form second panel (gameplay keys: isBoss/npc/fraction/
   immunities/resistances/battleMusic/attackRange/loot.category); level editor object/item
   placement tools; campaign per-level property editor.
   *(fraction/attackRange/carcassChance/spriteLayer/battleMusic/checks shipped 8407dfe39;
   campaign property editor + graph shipped round 6; open: immunities/resistances arrays,
   object/item placement)*
3. **Round 5c (infrastructure):** undo/redo for the level painter (snapshot stack is enough);
   cross-file search; autosave-or-explicit-dirty-badges; file copy UI; hero sprite descriptor
   route fix + shape gate for spritesDesc/items.json; fonts/jpg in save_binary allowlist
   (magic-sniff woff/ttf/jpg).

## 8. Round 6 (2026-11-17): campaign graph editor — live test ALL PASS

`levelsDesc/Dungeon.json` (and Dungeon_*.json variants) now routes to
`campaign-editor.js` — the "visual node editor" option from the round-5 report.
Live IAB pass against a seeded mod, all verified server-side after each step:

| Interaction | Result |
|---|---|
| Auto-layout by depth (nodes at predicted pixel coords), entrance styling, edge exit-index badges | PASS (canvas pixel readback) |
| Node select → property panel (kind select, depth, file datalist 39 opts, music datalist 61 opts, checks, size, exits/entered-from lists, Entrance select) | PASS |
| Property edit + save round-trip (depth/music/nMobs; isSafe + unknown keys preserved) | PASS (disk verify) |
| Anchor drag-connect (0→start, 1→start, start→new level) — exits + mirrored entrances written | PASS (disk verify) |
| Edge select via badge + Delete key → both halves removed (mirror dropped only when no parallel exit) | PASS |
| "+ level" (prompt shim) auto-connects from the selected parent | PASS |
| Rename id — Levels key order kept, all Graph refs + Entrance rewritten | PASS |
| "New level file for this node" — levelsDesc template file created + `file` key set | PASS |
| Double-click node → level painter; painter's "Open campaign graph" round-trips back | PASS |
| Node drag → localStorage layout → survives full page reload (pixel-verified) | PASS |
| Legacy 3-tuple Graph → bounces to json text editor (with dirty tracking via adoptTextFallback) | PASS |
| New E2E: campaign save round-trips + graph validation (dangling = error, graph-less level = warning, one-way = silent) | PASS (suite ALL PASS ×2) |

Found and fixed along the way: the round-5 `onSaved: refreshTree()` ReferenceError
(all editors — save succeeded but status lied and tree never refreshed), stale status
after edge delete, arrowheads painted under target nodes, and the shape gate missing
legacy 3-tuples. Traps this round: coordinate predictions must re-derive after any
depth edit (auto-layout recomputes); a lingering `window.confirm=true` shim from an
earlier cell auto-approves later Delete-key node deletions.

## 9. Round 8 (2026-11-17): mobile-first creation workflow live test

Walked the **from-scratch creation journey** at a 390x844 phone viewport (ZCode IAB,
fresh headless jar): dashboard create-form → new mob (json + sprite + png + strings) →
new level (touch-painted, mob placed) → campaign wiring → validate → fix loop →
"Play this mod" → in-game boot + level walk. Every save disk-verified; the game
booted the touch-built mod (hero town→start, l2 loads with TestMob at the placed
cell, authored HT 30, name string resolving). E2E suite ALL PASS on the same jar
before the manual pass.

**Works flawlessly on touch (all verified live):** create-from-template form; tree
tap-expand; root-level "+ new file" with full paths; mob form 4× edit+save
round-trips; painter touch strokes (disk-verified at exact cells), terrain/zoom/tool
selects, mob placement by tap (exact cell); campaign graph tap-select at predicted
auto-layout coords, "+ level" auto-wiring, per-node file datalist, save round-trip;
strings "+ pair"; sprite form with animation scaffolding; validation → in-form fix
loop (scriptFile cleared via form, key removed from json); Play-this-mod restart +
active-mod switch.

### New findings

| # | Finding | Priority | Evidence |
|---|---|---|---|
| M1 | **"+ new file" silently double-nests path-shaped names**: `onNewFile` prepends the dir unconditionally (`path = dir + '/' + name`, app.js) although the mob form's status line ("Not found: spritesDesc/MyMob.json") pushes users toward exactly such names. Typing `spritesDesc/TestMob.json` inside `mobsDesc/` created `mobsDesc/spritesDesc/TestMob.json` (empty file, mob form for a sprite path). Live-verified on two dirs. Fix: honor names that already begin with a known mod-dir prefix | **P2** | live + app.js |
| M2 | **New-mob template hardcodes `MyMob` into 6 fields** regardless of file name: name/name_objective/description/gender keys, `spriteDesc: spritesDesc/MyMob.json`, `scriptFile: scripts/mobs/MyMob`. Consequences (all live): fresh mobs ship a validation ERROR (scriptFile not found), in-game description shows the raw key `MyMob_Desc`, sprite preview fails (`mobs/my_mob.png`), gender is a junk key. templates.js already computes `kind` for placeholders — derive all six from it | **P2** | live + char_desc + validate |
| M3 | **Campaign "+ level" wires a logical exit with no physical door; validation blind**: template Start.json ships 1 exit cell; after "+ level" the graph has 2 exits. `get_exits` returns both, but the only physical cell leads to exits[0] — the new level is unreachable in-game while validate stays clean (validator checks exit-cell bounds only, never graph-exits vs physical-cell count). Minimum fix: warn "graph exit #N of <level> has no physical exit cell (paint one with the exit tool)" | **P2** | live: get_exits `[1, l2]` vs 1 cell; ModEditorEndpoints:1390-1408 |
| M4 | **"+ language" seeds from a stale snapshot**: `refMap` is fetched once at editor open (text-editors.js:224), so pairs saved earlier in the same session are missing from the seeded file. Live: en had 4 pairs at click time, strings_ru.json seeded with 3 — the new mob's name key silently absent → untranslated raw key in-game. Fix: re-fetch at click, or track added keys | P3 | live disk diff |
| M5 | Painter zoom select caps at 25px/cell (default 18) — a 15x15 map is 270px on a 390px phone, bigger maps need the pan tool; fingers want ≥32px options. Also the landing hint is a 9-line wall naming every editor (pushes the workflow below the fold on phones) and the `working…` chip overlaps content at the bottom | P4 | live |

### IAB automation traps added this round

- The IAB reloads the page **between evaluate calls and sporadically mid-evaluate**
  (module state, armed shims, drawer state all die). Every interaction step must be
  one atomic evaluate with sync shim+click blocks; shim-verify immediately before
  clicking (`if (!/X/.test(String(window.prompt))) return retry`).
- A real `prompt()` left pending wedges the dialog bridge progressively: first
  `getJsDialog()` still works (but **misreports prompts as type "confirm"** —
  accept-with-text never runs), then handleDialog itself times out. Recovery =
  close the tab and open a fresh one. A pending dialog can also get auto-answered
  with the last accepted text, re-running the file-creation handler later.

## 7. Continuation round (same day): wild-mod quantification + regression pass

Two more evidence passes, both against HEAD master.

### 7a. What real mods actually use (8 mirror mods, 92 mob files, 185 level files, 527 campaign entries)

Scanned the local mod mirror (`~/StudioProjects/remixed-dungeon-mods`, 8 published mods).
This re-ranks the gaps by real-world impact:

**Mob json (92 files)** — engine keys the form does not expose, files using them:
`fraction` 24/92 (26%!), `attackRange` 20/92, `battleMusic` 9/92, `movable` 5/92,
`immortal` 5/92, `battleMusicFallback` 4/92. `isBoss`/`npc`/`immunities`/`resistances`/
`carcassChance`/`loot.category` — 0 uses in the wild (still worth exposing, but P3).
The form's missing `fraction` alone silently affects a quarter of all published mob files.

**levelsDesc (185 files)** — `items[]` 95 files (51%!), `objects[]` 70 (38%),
`customTiles` 66, `baseTileVar` 57, `decoTileVar` 56, `deco2TileVar` 31. The round-4
WYSIWYG bet (customTiles + var layers) was exactly where the data is; the missing
object/item placement tools (c0i) cover the two most-used unmodeled keys.

**Dungeon.json Levels[] (7 mods, 527 entries)** — properties per entry:
`kind` 526, `depth` 525, `file` 377, `size` 343, **`music` 320, `script` 258,
`isSafe` 235, `nMobs` 187, `noBuild` 77, `noFogOfWar` 60**, `feeling` 25,
`maxBrightness` 23, `isStatic` 11, `viewDistance` 10, `tiles` 10, `objectsKind` 10,
`fallbackMusic` 10, `treasury` 9, `water` 1. The report's earlier campaign-property list
undercounted: add `nMobs`, `noBuild`, `feeling`, `isStatic`, `viewDistance`, `objectsKind`,
`fallbackMusic` to what a per-level campaign editor must expose (all beyond the
kind/depth/file that "Add to campaign" writes).

**initHeroes (8 files, per class sections)** — `knownItems` 16, `quickslot` 13,
`isSpellUser` 9, `immunities` 6, `forbiddenActions` 3, `left_hand` 2, `friendlyMobs` 2,
`startingSp` 1, `allies` 0. Note: **`isSpellUser` is authored by 3 mods but the engine
never reads it from the loadout** — `Hero.isSpellUser()` derives from
`magicAffinity` (Hero.java:1211), `Char` hardcodes true (Char.java:2398). The form's
checkbox is a no-op field today: either honor it engine-side or mark it as such in the form.

**Content presence (mods shipping it, /8):** `sound/` 7, `strings_ru.json` 7,
`levelObjects/` 5, `effects/` 4, `spritesDesc/items.json` 3, spells/buffs/actors/traps 3
each, `scripts/startup/` 2, `hero/spritesDesc/` 1. **Alchemy recipes, plantsDesc,
scripts/plants, painters, services, fonts, custom tilemapDesc: 0 mods ship any** — the
alchemy/quest editor gaps (5gh) are about lowering the barrier for future mods, not about
existing content; the sprite/items-map gaps (a3u) have live data behind them.

### 7b. Regression pass over the round-1..3 editors (shared code changed in round 4)

| Editor | Result |
|---|---|
| Mob form | PASS — 21 inputs, walkingType select; ht 9→11 round-trip; `fraction`/`isBoss`/`battleMusic`/`movable` all preserved (not shown in form — gap 71a confirmed live) |
| Sprite form | PASS — texture prefilled from json, 2 canvases (preview + sheet) |
| Object editor | PASS — script field parsed, sheet preview canvas renders `levelObjects/barrels.png` frame |
| Effect editor | PASS — fps:10 + frames parsed, animated preview canvas |
| Spell form | PASS — 10 fields parsed from lua (name, cooldown 5, image…) |
| Buff form | PASS — 3 fields parsed |
| Rename via file-tree UI | Works server-side (409/round-trip already in E2E); **the tree does not re-render the new name after rename** — stale until next refresh (P4 evidence for the onSaved no-op gap) |
| "+ new file" name-aware templates | PASS — a new `levelsDesc/QaTimingTreasury.json` gets the treasury template and opens directly in the treasury form (v1 shape, 7 inputs) |

New live-test trap: `dirNode` lis carry no `dataset.path` (only file lis do), so UI-locator
tests must target dir labels by their `name + '/'` text.


## 10. Round 9 (2026-11-18): gap-fill — all P2/P3 findings closed

Mobile-first round: every finding from sections 9/3 fixed and live-verified on the
headless server via browser GUI + API reads. bd dv5/53c/igf/mvn/71a/a3u/5gh/33n closed.

| Fix | Verification |
|---|---|
| dv5 "+ new file" double-nesting: path-shaped names (`spritesDesc/X.json`) typed inside a dir are now mod-root-relative | GUI: created from mobsDesc/ → landed at root spritesDesc/, sprite form opened |
| 53c mob template: all authored keys derive from the file base name; `scriptFile` and junk `gender` dropped | GUI + API: fresh QaMob.json validates without scriptFile error; fields QaMob_Name/…_Desc/spritesDesc/QaMob.json |
| 71a remainder: immunities/resistances comma-list fields (datalist of builtin kind names) + undead/demon checks + level | GUI + API round-trip: `immunities: [Poison, Sleep]`, `resistances: [Fire]`, `undead: true` |
| igf unreachable exits: validate warns when Graph exits > physical exit/multiexit cells; campaign panel shows "N graph exit(s) · M physical exit cell(s)" | E2E both directions; GUI-built over-exit campaign (start: 2 exits, 1 cell) warns verbatim |
| a3u: `hero/spritesDesc/` opens the sprite form; `spritesDesc/items.json` gets a per-item form ({kind: {file, index, fliesStraight}}) + sheet-shape gates both ways | GUI: hero sprite form opens; items round-trip `QaDart: {file: items.png, index: 3, fliesStraight: true}` |
| 5gh: alchemy_recipes.json form editor (collapsible recipes, input/output stacks) + `scripts/quests/` quest-giver snippet (modeled on QuestGiverDemo.lua) + modding.md §Alchemy | GUI: 2 recipes round-trip; snippet + zoom served |
| mvn stale refMap: "+ language" re-reads strings_en.json at click time (+ merges unsaved rows when en itself is open) | GUI: pair added+saved, same-session +language seeded all 4 keys |
| 33n: painter zoom 40/48px, landing hint = 4-step checklist + collapsible editor map, busy chip in-flow under topbar on narrow screens | GUI snapshot (landing, zoom via served asset); busy chip CSS-only |

Round-9 trap found while testing: `spritesDesc/items.json` initially received the
*sheet* template (prefix match) and the items form mis-rendered its anim objects as
item rows — fixed with exact-name template picks before prefix matching + a
`texture`-key gate that bounces sheet-shaped items.json to the json text editor.

Still open from section 3-4 (bd 5dr): undo/redo, cross-file search, autosave, file
copy UI, fonts/jpg save_binary allowlist, strings side-by-side view.
