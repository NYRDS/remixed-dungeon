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
| **Campaign entries**: "Add to campaign" writes only kind/depth/file (level-editor.js:405-409); per-level properties `noFogOfWar`, `isSafe`, `music`, `script`, `maxBrightness`, `treasury`, `size` need raw Dungeon.json editing | **P2** | levelsDesc/Dungeon.json:3-15 |
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
| 30 s client abort on every request — `clone_builtin` (~1.5k files) can exceed it client-side while the server finishes fine | P3 | api.js:11 |
| `onSaved` callbacks are no-ops — file tree doesn't refresh after saves that add/rename files; `files` size field never rendered | P4 | app.js:474, file-tree.js:66-105 |
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
2. **Round 5b (forms):** mob form second panel (gameplay keys: isBoss/npc/fraction/
   immunities/resistances/battleMusic/attackRange/loot.category); level editor object/item
   placement tools; campaign per-level property editor.
3. **Round 5c (infrastructure):** undo/redo for the level painter (snapshot stack is enough);
   cross-file search; autosave-or-explicit-dirty-badges; file copy UI; hero sprite descriptor
   route fix + shape gate for spritesDesc/items.json; fonts/jpg in save_binary allowlist
   (magic-sniff woff/ttf/jpg).
