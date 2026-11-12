# Mod editor live test report (desktop, 2026-11-09)

Scope: live end-to-end test of the built-in mod editor (merge 9bca81dd8) on the
desktop build — web UI driven in a real browser (IAB/Chromium 1280x720), server
= DesktopLauncher `--webserver=8080 --windowed --nosound` from
`RemixedDungeonDesktop/src/desktop/rundir`, master @ 9bca81dd8 (compiled fresh).
Test mod: `rundir/mods/LiveTestMod` (created from template via the UI, left in
place as evidence; safe to delete). This complements
`tests/http_api/test_mod_editor.py` (API level) with UI-level + in-game checks.

## Verdict

Core editing loop is solid: create mod → edit mob json (form) → paint a level →
edit strings → validate → save → **level loads in the live game exactly as
edited** (verified: resized 16x15 map, painted terrain, exit tile registered
`isExit(23)=true` in-game, descend to next level works). Unknown-key
preservation (ba9a4f0fa P2 fix) re-verified live on the mob form.

But 3 P1 bugs block the "new modder" happy path, and the dashboard is missing
several affordances the API already supports.

## P1 — functional bugs

### 1. `/api/mods/raw` returns 0 bytes for every builtin-fallback file
`ModEditorEndpoints.handleModsRaw` (RemixedDungeon/src/main/java/com/nyrds/platform/app/ModEditorEndpoints.java:275)
wraps the fallback stream in try-with-resources and then hands the *closed*
stream to `newChunkedResponse` — NanoHTTPD reads lazily at serve time, gets
"Stream Closed", client receives HTTP 200 with an empty body. The mod-file
branch (line 270) returns the open stream directly, which is why mod-local
files work.

Evidence (live):
- `curl /api/mods/raw?mod=LiveTestMod&file=rat.png` → `200, 0 bytes`;
  same for `air_elemental.png`, `items/food.png`. Missing file correctly 404s.
- `save_binary`-written mod-local `bat.png` → `200, 589 bytes` (branch works).
- stderr spammed with `SEVERE: Could not send response to the client -
  java.io.IOException: Stream Closed` from NanoHTTPD.

UI impact:
- Mob form sprite preview (`mob-editor.js` refreshPreview) is a broken image
  for every mob referencing a builtin spriteDesc texture — i.e. the template
  ExampleMob (spritesDesc/Rat.json → builtin rat.png) out of the box.
- Level editor tileset picker loads nothing for builtin tilesets
  (`tilesets/tiles_sewers*.png`) → tile-var painting unusable until the modder
  manually acquires a tileset — and there is no UI to copy/upload one (see P2-3).

Fix: buffer the bytes before building the response (like handleModsRead does
with readStream), or drop try-with-resources and pass the open stream, matching
BaseWebServer.java:717/1109/1140/1279.

### 2. Template campaign is a dead end: Start room has no exit
`assets/mod_editor_template/levelsDesc/Start.json` declares `entrance` but no
`exit`/`multiexit`. `PredesignedLevel` only creates exits from json
(com/nyrds/pixeldungeon/levels/PredesignedLevel.java:104-114) — nothing auto-
places one. Live-verified on the shipped template: hero spawns on `start`
(levelId=start), `lvl.exit=nil`, zero exit tiles (26/27/28) in the map, no
objects/NPCs — the player can never leave the first room, so `Graph start→1`
is unreachable. The builtin pattern the template must mirror has them:
`Town_2021_03.json` carries 7 `multiexit` entries.

The http_api test never caught this because it asserts landing + mob spawn,
never progression.

Fix: add e.g. `"multiexit": [[7, 1]]` to the template Start.json (verified
in-game: with that entry, `isExit` true and descend works). While there,
consider shipping a ExampleMob in the room (template currently has no mobs —
the "starter creature" strings keys exist but nothing spawns).

### 3. Desktop "Play this mod" restart permanently kills the webserver
`restartApp` (RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/app/RemixedDungeonApp.java:171)
spawns the new java process and only then `System.exit(0)` — the new process
boots while the old one still holds :8080, its NanoHTTPD dies with
`java.net.BindException: Address already in use` (3/3 restarts hit it during
this session), and the game comes up webserver-less. The editor page's
`setTimeout(reload, 4000)` then lands on a dead port: user loses the editor
(and the whole debug API) until the game is manually relaunched. The mod
switch itself works — after a manual relaunch the game booted straight into
LiveTestMod (prefs persisted correctly).

Headless is unaffected (doRestart = process exit; the test runner relaunches).

Fix directions: stop the webserver (free the port) before exec'ing the new
process; or retry-bind loop with backoff in the new process; or make the web
app poll instead of a single 4s reload (helps but does not fix the dead
server).

## P2 — feature gaps in the UI (API exists, UI doesn't)

1. **Built-in Remixed mod invisible in the dashboard.** `handleModsList` only
   lists installed mod dirs, so after "Play this mod" there is no way back to
   Remixed from the editor (and before switching, the active builtin mod isn't
   shown either). Suggest a read-only "Remixed (built-in) ● active" row with a
   Play button, mirroring WndModSelect.
2. **No Duplicate / Delete mod buttons.** `duplicate`/`delete` endpoints + 
   `api.duplicateMod/deleteMod` are wired in api.js but the dashboard renders
   only Edit + create form.
3. **No image add/upload path.** `save_binary` (png-only, magic-checked) has no
   caller in the editor UI; `imageEditor` is preview-only and delegates to
   mob-viewer, which saves into the *active* mod only. A new mod cannot get
   sprite art without the activate→mob-viewer detour, and new-file `x.png`
   creates a 0-byte file that previews as a broken image (no graceful message).
4. **No file rename UI.** `rename_file` endpoint + `api.renameModFile` exist;
   `file-tree.js` documents `opts.onRename` but never renders a rename control
   (only delete ×).

## P3 — smaller issues

- `templates.js` is dead code: `TEMPLATES`/`templateFor` are never imported;
  app.js `onNewFile` always saves empty content. New `scripts/mobs/X.lua` etc.
  start blank — either wire the templates into onNewFile or delete the file.
- Export button uses `window.open(url, '_blank')` with no feedback; in popup-
  blocking/embedded browsers it silently does nothing (observed in IAB). The
  endpoint itself is correct (zip has top-level `<mod>/`, 9 files, reinstall
  shape). A hidden `<a download>` click + status text would be robust.
- `api.request` has no timeout — one observed hung POST (select over a reused
  keep-alive connection in IAB) left the page stuck with no error path. Fetch
  timeout + error status would harden the restart flow.
- Level editor "var pick" row renders as an unlabeled empty box in the tool
  panel (screenshot review; the `pick -1 (clear)` button text is not visible —
  css/layout nit worth a look).
- Validation gaps: orphan level files not referenced from Dungeon.json Levels
  are not flagged; entrance/multiexit placed on wall/border cells are accepted
  (no bounds/passability check); no warning when a mob's spriteDesc resolves to
  a *builtin* sprite (new modder's mob silently looks like a rat).
- `Dungeon.json` Graph editing is raw json only — creating a new level file and
  wiring it into Levels+Graph is fully manual; a "add level to campaign"
  helper is the biggest remaining editor UX gap after the P1s.
- mob form: drops arrays/tags/custom props are preserved but not editable (by
  design, matches Mob.fillMobStats scope) — noting as a future field-set.
- WndModSelect "Mod editor" button hardcoded EN (documented precedent, listed
  for completeness).

## Verified working (live)

- Create from template (files + generated version.json with title/author/desc);
  duplicate-name create → 409 surfaced in UI status line.
- Dashboard list + `● active` marker; Edit navigation.
- File tree: lazy dirs, + new file (root and per-dir), delete via × with
  confirm, refresh without page reload.
- Text editors: json (client pretty-print, server re-validation → 400 on bad
  json), lua (server compile → refused with "Lua error ... syntax error"),
  strings pairs editor (add row, save, per-line format correct on disk),
  Dungeon.json correctly opens as text (canvas editor exclusion works).
- Mob form: full render (P1 IDL fix holding), names/gender/stats/checkboxes/
  walkingType/aiState/loot, spriteDesc datalist (mod + builtin listing), script
  datalist (90 builtin entries), save preserves unknown keys live-verified
  (tags/drops/customProp survived; ht/canBePet written).
- Level editor: terrain paint, mob place (dup-cell guard), exit toggle on/off,
  erase-mob, resize 15→16 (map + var layers remapped, mobs/exits filtered,
  result loaded by the live game as 16x15), map-length save guard, unknown
  keys preserved; single click = single placement (double-fire suspicion
  disproven; one intermittent double-toggle observed under synthetic input —
  worth a dedup guard someday, not user-reproducible).
- Validate panel: per-file json/strings errors with paths, missing level file
  (`level broken: file not found`), lua compile, stats line, busy-guard.
- Export zip shape (curl-verified; top-level dir = UnzipTask-compatible).
- Select+restart end-to-end: game switched into the editor-created mod and
  booted into it; editor→disk→game round-trip proven with the exit tile.
- Zero third-party requests; page fully operable in a plain embedded Chromium.

## Environment / repro notes

- Desktop launch recipe as in desktop-only-build-workspace memory (rundir cwd,
  assets first on -cp, /tmp/rpd-desktop-cp.txt classpath dump).
- `/api/mods/*` writes go to `rundir/mods/<Mod>/` (desktop external storage).
- Browser-side: page-side `evaluate` clicks are the reliable drive path;
  window.confirm blocks evaluate (handle getJsDialog in a separate call);
  window.prompt was silently auto-dismissed in IAB (shim `window.prompt` to
  test the new-file flow).

---

## Round 2 (2026-11-11) — gaps filled

All P1s and P2s from this report addressed on master; editor now covers
map / mobs / sprites / tiles / items / scripts.

| item | outcome |
|---|---|
| P1-1 raw endpoint 0 bytes | fixed 9300bd166 (buffered fixed-length response); png-magic test added |
| P1-2 template dead end | NOT REPRODUCIBLE - template already carries `multiexit [[7,1]]`; verified in-game: exit tile + descend start→1 work (bd k8t closed) |
| P1-3 restart kills webserver | fixed b14802970: stop-before-exec + probe-retry bind (nanohttpd 2.3.1 binds on its listener thread and dies silently - a plain start() can't be trusted) + listener verify + UI poll-reconnect; race-verified headless |
| P2-1 builtin invisible | `list` synthesizes a Remixed row; Play/Restart on every dashboard row |
| P2-2 duplicate/delete | dashboard buttons, live-verified |
| P2-3 image upload | shared uploadPng (bytes passthrough, no re-encode): sprite editor, png viewer replace, tileset copy-into-mod (byte-identical verified) |
| P2-4 rename | ✎ glyph in file tree, round-trip verified |
| P3 templates dead | wired into new-file; + level template (15×15, entrance+exit) |
| P3 export window.open | hidden `<a download>` |
| P3 fetch timeout | 30s AbortController in api.request |
| P3 var-pick row | renders labeled (label + button text present in DOM) |
| P3 validation gaps | warnings now RENDERED; builtin-sprite-desc warning, entrance/exit out-of-bounds/on-wall warning, orphan level file warning |
| P3 campaign wiring | "Add to campaign" in level editor: Levels+Graph edit with unknown keys preserved; verified in-game start→1→cellar (editor-made level, PredesignedLevel depth 2) |

New editors: spritesDesc form + animated preview (TextureFilm frame math,
verified against builtin rat.png); scripts/items form over the lua desc
table (surgical rewrite, non-scalar fallback to text editor).

Stale-asset trap found live: /web/ responses now send Cache-Control:
no-cache (editor UI ships inside the game and changes under the browser).

test_mod_editor.py ALL PASS (raw checks + 2 restart cycles).
test_path_traversal.py: cannot initialize its gradlew GL-desktop server
in this environment (pre-existing); mod-scoped traversal guards covered
by test_mod_editor.

## Round 3 (bd b4i, 2026-11-12)

Scope: edit downloaded mods under their real dir names, clone builtin
content, sound assets, object/effect/spell/buff editors, design doc
(`mod_editor_design.md`).

| item | outcome |
|---|---|
| downloaded mods un-editable | `isValidModName` rejected every installed dir with spaces/dots ("Remixed RPG", "D.U.N.G.E.O.N") - list/files/save/export/duplicate all 404'd. Split: existing mods addressed by real dir name (single path segment inside storage root), strict charset kept for NEW names only |
| hash routing | `#/mod/Space%20Mod` encode/decode; crumbs/tree/edit/save verified live in IAB |
| mod-name traversal | `..`, `a/b`, `../Mod` → 404 (tested) |
| clone builtin | POST `clone_builtin`: whitelisted content dirs + root pngs (~1557 files, 6.6 MB), web apps/bootstrap excluded, fresh manifest, clone validates clean; budgets (depth 16 / 5000 files / 128 MB) + partial-failure cleanup |
| listResources trap | a REJECTING FilenameFilter makes ModdingMode DESCEND into rejected dirs (walked `./mods` = the clone itself on headless/desktop). Clone uses accept-all listings + client-side filtering only |
| builtin mobs with dangling spriteDesc | ArmoredStatue/Yog/MirrorImage ship NO spriteDesc and no default file - clone-validate flagged them. Rule now: explicit spriteDesc missing = error; unauthored + missing = warning (engine fallback sprite) |
| sound assets | save_binary accepts png/ogg/mp3/wav magic-sniffed; audio editor (playback + replace); tree "⇧ upload file" per dir; raw serves audio/* mime |
| spell editor | form over `desc()` table (shared lua-desc-table module with item editor); datalists (16 icon sheets, affinities, targeting); save round-trip verified live (cooldown rewrite, cast fn intact) |
| buff editor | desc-table form (icon/name/info) |
| object editor | levelObjects json form + click-to-pick sprite sheet preview + script/texture datalists; unknown keys preserved |
| effect editor | effects json form + live animated preview (frame stepping verified via canvas readback) |
| templates | + spells, traps, objects, npc, actors, levelObjects, effects, mobsDesc, spritesDesc |
| P1 found live (pre-existing disk drift) | file-tree.js had uncommitted `'../api.js'` import (HEAD has `'./api.js'`) - the whole app failed to load; node --check can't catch specifier resolution, only a real browser pass does |
| IAB page reloads between evaluates | each browser check must be atomic (goto + one evaluate); hash changes inside evaluate break the inspection context |

Gates: test_mod_editor.py ALL PASS (incl. new sections); android fdroid
compile GREEN; test_path_traversal.py PASS this round (its earlier
gradlew-GL wedge did not recur); LanguageTool pass on new UI strings
(2 "builtin"→"built-in" fixes, rest identifier false positives).
