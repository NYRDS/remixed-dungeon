# HTML (TeaVM) port — resume plan

Branch: `html-port-runnable` (work in progress, see git log)
Serving setup: `python3 RemixedDungeonHtml/make_webapp.py --skip-build` then
`python3 RemixedDungeonHtml/serve.py --port 8081` → http://127.0.0.1:8081

## Current state (as of 2026-09-06)

- `RemixedDungeonHtml:compileJava` ✅, `generateJavaScript` ✅ (~38 MB debug /
  ~7.7 MB obfuscated teavm-app.js), desktop ✅ (Java 11), android fdroid ✅.
- Game in browser boots through: main() → preload screen → all 1303 assets →
  shader compile → real textures → LuaEngine init → ~59 luajava bindClass calls.
- **Blocker**: lua `require` still dies in `luajava.newInstance` →
  `LuajavaLib_invoke` → `getConstructor()` returns null for some class
  (constructor metadata not emitted). Title screen not yet reached. Error count
  grows per frame (123 at last check) — the exception repeats each render tick.

## Immediate next steps

1. Identify the class failing `getConstructor()`:
   - `PlatformLuajavaLib.classForName` logs every bindClass request to the
     browser console (`LUAJAVA: classForName: ...`, via `window.__errors`).
     The last logged name before the crash is the module being loaded; the
     crash itself is a `luajava.newInstance` on some class.
   - Add logging around newInstance (override `LuajavaLib` NEWINSTANCE path or
     log in `classForName` callers).
2. Constructor metadata: TeaVM emits reflection metadata per class from the
   `ReflectionSupplier` output (`<init>` MethodDescriptors included). If the
   failing class is in `lua-interface-map.json` but its ctor is still null,
   check `ReflectionDependencyListener.handleClassNewInstance` flow — possibly
   the class needs `agent.linkClass` before metadata is emitted (we already do
   that via `getClassesFoundByName`), or the ctor body references something
   unlinkable and TeaVM silently dropped it.
3. After lua loads: expect further boot errors (title scene render, save/load,
   Preferences on localStorage). Iterate the same loop:
   reproduce → read `window.__errors` (`CAUSE-JSSTACK` = JS stack of the
   innermost Java cause) → fix → rebuild (`generateJavaScript`) → 
   `make_webapp.py --skip-build` → browser reload.
4. Once title screen + new-game works: remove debug breadcrumbs, rebuild with
   `-Pteavm.obfuscated=true` (build.gradle reads it, default false for now),
   commit, push.

## How to debug this port (hard-won notes)

- **Boot hooks**: `make_webapp.py` embeds an inline script BEFORE
  `teavm-app.js` capturing `window.__errors` (with `e.error.stack`) and
  `window.__logs`. `Game.render` (html shim) dumps the innermost cause's raw
  JS stack via `$jsException` (`CAUSE-JSSTACK:` lines) — TeaVM's own
  `getStackTrace()` is always empty.
- **`CAUSE-JSSTACK` positions** map to `teavm-app.js` only in the
  unobfuscated build (`obfuscated = false` is the current default in
  RemixedDungeonHtml/build.gradle via `-Pteavm.obfuscated`).
- **System.err reaches the browser console; System.out does not.**
  EventCollector (html shim) prints via System.err → visible.
- **Delombok is stale-prone**: main-source edits land in
  `RemixedDungeonHtml/build/delomboked/` — if output looks old, check there.
- **hjson is unusable on TeaVM** (its regexes use `\x{...}` escapes) —
  `Util.sanitizeJson` now tries strict JSON first, hjson as fallback.
- **`BOMInputStream.close()` throws on TeaVM** even after a successful read —
  `JsonHelper.readJsonFromStream` treats a close failure with content in hand
  as non-fatal; keep that structure.
- **TeaVM reflection** (enableRef=true): `Class.forName` finds only classes
  TeaVM linked. `LuaReflectionSupplier` (in :processor,
  META-INF/services/org.teavm.classlib.ReflectionSupplier) declares all
  `lua-interface-map.json` classes via `getClassesFoundByName` and exposes
  their members. If new lua-bound classes appear, annotate them with
  `@LuaInterface` (method-level annotation puts the class in the map).
- **Classes that reference `java.text.*` / `java.util.logging.*`** must stay
  out of reflection metadata AND out of reachable-from-lua code — TeaVM cannot
  link those packages (no TCollator/TFileHandler). App code was moved to
  String.CASE_INSENSITIVE_ORDER / GLog+FileWriter.
- **Mixed versions**: gradle plugin is upstream 0.13.1 (teavm.org), classlib is
  the local fork (mavenLocal, commit 52f17f621 with stubs). `js.obfuscated`
  property from the CLI (`-Pjs.obfuscated=false`) does NOT work; use the DSL
  `obfuscated = ...` in build.gradle (done).
- The `teavm` and `luaj` git submodules are pinned and clean; luaj had
  `os.execute` stripped (JseProcess excluded from jar, JseOsLib returns
  EXEC_ERROR).

## Tooling

- `RemixedDungeonHtml/make_webapp.py` — assembles `build/webapp`: teavm-app.js,
  index.html (with hooks + main() call), assets/ + assets.txt manifest
  (type:kind:path:length:overwrite), `scripts/gdx.wasm.js` +
  `assets/startup-logo.png` extracted from backend jar, gdx classpath
  resources. `--skip-build` reuses the last generateJavaScript output.
- `RemixedDungeonHtml/serve.py --port 8081` — no-store static server.
- Browser test loop: reload → evaluate `window.__errors` / `window.__logs`.
- Three-platform compile check:
  - html: `./gradlew -c settings.html.gradle :RemixedDungeonHtml:generateJavaScript`
  - desktop: `./gradlew :RemixedDungeonDesktop:compileJava`
  - android: `./gradlew -c settings.android.gradle :RemixedDungeon:compileAndroidFdroidDebugJavaWithJavac`

## Commits on this branch (topic → files)

1. `fix: html build - scope desktop excludes...` — compile fix.
2. (uncommitted, squashed by topic at commit time — see git log) TeaVM boot
   fixes, asset pipeline, lua reflection supplier + @LuaInterface sweep,
   TeaVM-compat app fixes, webapp tooling.

## Open questions / later

- Sound: html audio shims are stubs; `isSoundExists` returns true blindly.
  Low priority per goal.
- Mods on web: `listResources` returns empty; mod loading unexplored.
- Saves: HtmlPreferences/localStorage — untested.
- `index.html` in `RemixedDungeonHtml/src/main/webapp/` is stale GWT-era junk
  (the real one is generated by make_webapp.py).
- Consider upstream TeaVM master (2f378217c) — has Integer.sum etc.; we fixed
  app-side instead. Rebasing the fork's stubs is possible but risky.
