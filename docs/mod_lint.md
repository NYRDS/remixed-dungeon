# mod_lint — Remixed Dungeon mod linter

`mod_lint.py` lives in the mod-mirror workspace
`/home/mike/StudioProjects/remixed-dungeon-mods/` (alongside the unpacked
official mods, their git clones in `repos/`, and `chunk_dl.py`). It validates
third-party mods against the *actual* engine parsing rules, several of which
were only discovered by reading engine code while building the tool.

## Usage

```bash
python3 mod_lint.py <file-or-dir>... [--fix] [--quiet]
```

Default mode is a check (exit 1 if anything is not clean); `--fix` rewrites
in place. Output marks: `ok`, `MESSY` (auto-fixable or report-only), `FIXED`,
`ERROR` (needs human judgement), `REF` (hard cross-reference failure),
`INFO` (soft findings, aggregated).

## Function 1: messy JSON → strict JSON

The engine parses mod JSON as **HJSON** (`Util.sanitizeJson` →
`JsonValue.readHjson`) after BOM stripping, so mods ship BOMs, `/* */` and
`//` comments, trailing commas, unquoted keys and even `key = value`
separators — which hjson itself rejects, making those files dead in
`JsonHelper`'s fallback chain.

- BOM stripped (the tool **never** writes a BOM); comments removed
  string-aware, newlines preserved so line numbers survive; trailing commas
  dropped. Surgical first: everything else stays byte-identical.
- `key = value` files get `=` → `:` then a full hjson → strict reformat
  (only when surgical fixes cannot make the file strict).
- Every rewrite is re-validated with the strict stdlib parser before saving.

Special case: **`strings_<lang>.json` is not a JSON document.**
`StringsManager.parseStrings` reads it line by line, each line must be a JSON
array; only 2-element `[key, value]` lines are applied. Such files are cleaned
per line (trailing commas, broken `\` escapes, raw control chars) and never
merged into one document.

## Function 2: Lua syntax + format

- Syntax: system `luac` (5.1) first; every rejection is re-checked against the
  **engine's own LuaJ `luac` jar** (`luaj/build/libs/luaj-jse-*.jar`), which is
  the authoritative oracle. Two traps learned the hard way: LuaJ accepts
  C-style float literals (`0.01f`) that luac 5.1 rejects, and the LuaJ `luac`
  **exits 0 even on compile errors** (judge its output, not the exit code).
  Run it with a scratch cwd — it can litter `luac.out`.
- Format auto-fixed: BOM, CRLF, trailing whitespace, missing newline at EOF.
  Mixed tab/space indentation is reported only (per-file dominant style is
  never rewritten).
- Lua syntax errors are never auto-fixed.

## Function 3: cross-references & data sanity (per mod root)

- JSON field paths (`texture`, `spriteDesc`, `scriptFile`, `tiles*`, `water`,
  level `file:`, `treasury`, …) must resolve in the mod, a nested content dir,
  **or the engine assets** (mods freely reference built-in music/tilesets).
- `music`/`battleMusic`/`fallbackMusic` resolve per
  `ModdingMode.getSoundById`: the engine looks up `sound/<id>.ogg` then
  `.mp3`, **appending** the extension — a value that already carries one can
  never resolve. Values are checked after stripping it.
- Lua `require "scripts/..."` targets must exist (dynamic requires like
  `"scripts/items/" .. name` are skipped; engine lib whitelist is read from
  `RemixedDungeon/src/main/assets/scripts/lib`).
- `textById`/`maybeId` string literals are checked against mod
  `strings_*` + engine `strings_all.xml` (INFO-level; engine lookup has
  fallbacks).
- Level maps: `width * height == len(map)`, entrance/exit in bounds.
- Duplicate JSON keys (both parsers silently keep the last), stringified
  booleans (`"noFogOfWar":"true"`), `version.json` sanity, junk/backup files
  (`* (1).*`, `*.bak`, `luac.out`, `Thumbs.db`), non-UTF-8 text files.
- Case-insensitive fallback lookup: a miss that matches a file differing only
  by case is tagged `case mismatch` — these work on Windows dev machines and
  break on the case-sensitive Android APK asset zip.

## Findings this tool produced (2026-09-10 pass over the 8 official mods)

- 283 JSON files strictified; 3 author-typo files repaired
  (`"level":3"`, `falce`, stray `]`).
- 11 engine-confirmed broken Lua scripts: 9 repaired, 1 deleted
  (unreferenced WIP duplicate), 1 truncated (abandoned multiplayer
  experiment, `SClientLua` consumer).
- 29 wrong-case level-registry references fixed (worked on Windows, broken
  on Android).
- 805 cross-reference issues catalogued (`lint_report_2026-09-10.txt` in the
  workspace): absent layer tilesets (`Mobs.png`/`Objects.png` in Remixed
  Additions), extension-bearing music values, missing treasury/level files,
  junk backups.

## Scope notes

- Repairs land in the `repos/` clones (pushed to NYRDS as
  `Mikhael-Danilov`); the unpacked zip mirrors stay as distributed.
- The engine's `com.nyrds.pixeldungeon.networking.SClientLua` is now a pure
  compatibility stub (removal targeted end of 2026) — its last consumer was
  the removed multiplayer experiment.
