# Built-in mod editor

A mod editor shipped inside the game, served by the embedded web server at
`/web/mod-editor/`. It edits **user mods** (the built-in Remixed mod stays
read-only; on dev platforms sprite writes remain available through
mob-viewer, as before).

## Opening it

- In game: Title screen → Mods → **Mod editor** (opens the browser at the
  server address; requires the web server, see the button's hint when it is
  not running).
- Directly: with the server running, open
  `http://<device-ip>:8080/web/mod-editor/` in any browser on the LAN.

## What it does

| Area | Notes |
|---|---|
| Create mod | Dashboard "New mod" - scaffolds `mods/<name>/` from the built-in template (an ExampleMob with the builtin rat sprite, a Start room level, a two-level campaign, strings stub) |
| Clone builtin | Dashboard row of the built-in mod: "Clone as editable mod" copies the whole game content (~1.5k files) into a new mod - the starting point for remixing the base game |
| Downloaded mods | Mods installed from the browser are edited under their real dir names ("Remixed RPG", "D.U.N.G.E.O.N") - spaces and dots included |
| Mob editor | `mobsDesc/*.json` open as a form: stats, strings keys, gender, walking type, ai state, loot, spriteDesc picker with live sheet preview, scriptFile picker |
| Sprite editor | `spritesDesc/*.json` form: texture, frame size, per-animation fps/loop/frames with an animated preview; png upload for sheets |
| Item / spell / buff editors | `scripts/items|spells|buffs/*.lua` open as a form over the `desc()` table (icon picker for items/spells); the rest of the script is untouched; non-standard shapes fall back to the text editor |
| Object editor | `levelObjects/*.json` form: kind, script, layer, appearance strings + sprite with a click-to-pick sheet preview |
| Effect editor | `effects/*.json` form: texture, frame size, anim - with a live animated preview |
| Level editor | `levelsDesc/*.json` (map files) open as a paintable grid: terrain palette (values straight from `Terrain.java`), entrance/exit placement, mob placement (this mod + builtin kinds), tile-var painting from a tileset preview, resize, "add to campaign". Terrain is color-coded, not WYSIWYG - the game's auto-tiling happens at render time |
| Strings | `strings_*.json` edit as key-value pairs (per-line `["Key", "value"]` format) |
| Sound | `sound/*.mp3/ogg/wav` play in the editor and can be replaced/uploaded (magic-checked); every dir has a "upload file" tree action for binary assets |
| Everything else | Text editors (json/lua) with server-side validation: json must parse (strict or hjson), lua must compile, strings must be well-formed pairs. New-file templates exist for items, spells, buffs, mobs scripts, ai, traps, objects, npcs, actors, levels, mobsDesc, spritesDesc, levelObjects, effects |
| Validate | Lints the whole mod: every json parses, lua compiles, mob chains resolve (`mobsDesc → spritesDesc → texture`, scriptFile), campaign `file` refs exist, `map.length == width*height` |
| Export | Downloads `<mod>.zip` with the mod dir at the zip root - the exact shape the regular mod-install flow expects |
| Play | "Play this mod" switches the game to the mod (save slots swap, same as the mods window) and restarts it |

Design doc with the full moddable-aspects coverage matrix:
`mod_editor_design.md`.

The template lives in `RemixedDungeon/src/main/assets/mod_editor_template/`;
`version.json` is generated per-create (name/author/description + current
`rpd_version`).

## Server API (`/api/mods/*`)

The legacy save endpoints write into the *active* mod only; the editor needs
to target any installed mod, so all editor endpoints take an explicit mod.

GET: `list`, `files?mod=&path=[&builtin=1]`, `read?mod=&file=[&builtin=1]`,
`raw?mod=&file=` (bytes, for img/canvas/audio), `export?mod=`, `terrain`.

POST: `create`, `clone_builtin`, `duplicate`, `delete`, `save`,
`save_binary` (png/ogg/mp3/wav, magic-sniffed), `delete_file`,
`rename_file`, `select`, `validate`.

Guards mirror the existing endpoints: NEW mod names must match
`[A-Za-z0-9_-]+`, but an INSTALLED mod is addressed by its real dir name
(downloaded mods ship spaces/dots) - either way: single path segment
only, resource paths are relative-only (`isSafeResourcePath`),
destinations must stay inside the storage root, the built-in mod is
read-only unless the platform opts in (`canWriteMainMod`, false on Android).
On headless, "select + restart" exits the process on purpose
(`Game.doRestart` = `System.exit`) - a launcher script restarts the jar.

## Test

`tests/http_api/test_mod_editor.py` runs the full loop against the headless
jar: create → edit → validate → guards → export → select+restart →
start_game lands in the template Start room with ExampleMob → switch back →
delete.

```
./gradlew :RemixedDungeonHeadless:headlessShadowJar
python3 tests/http_api/test_mod_editor.py
```
