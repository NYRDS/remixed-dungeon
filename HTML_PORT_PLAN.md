# HTML (TeaVM) port — resume plan

Branch: `html-port-runnable` (work in progress, see git log)
Serving setup: `python3 RemixedDungeonHtml/make_webapp.py --skip-build` then
`python3 RemixedDungeonHtml/serve.py --port 8081` → http://127.0.0.1:8081

## Current state (as of 2026-09-06, session 2)

- **TITLE SCREEN RENDERS AND RUNS** in the browser at ~58fps: lua boot
  completes, title scene draws (logo, archs background, buttons), zero
  uncaught errors, error count stable.
- Compile ✅ html/desktop/android. teavm-app.js ~38MB debug (obfuscated=false).

## What was fixed this session (in boot order)

1. **@LuaInterfaceProcessor dropped enums** — getEnclosingClass didn't accept
   ElementKind.ENUM → Fraction/Sample/MusicManager missing from the map.
2. **Missing @LuaInterface**: WandOfBlink/Telekinesis/Firebolt (newInstance'd
   from scripts/lib/commonClasses.lua:127-132 — the first newInstance crash),
   LuaWndBagListener, html Sample/MusicManager/ModdingMode (annotation was on
   the LuaError stub by mistake!)/BitmapData. Diff tool: extract
   `luajava.bindClass/newInstance("...")` literals from scripts/**/*.lua vs
   the generated map.
3. **Supplier exposed native methods** → "Native method has no implementation"
   (HtmlPreferences → JSONObject → Method.invoke links every static of every
   map class incl. ModdingMode.jsLog). Filter ElementModifier.NATIVE.
4. **lua-interface-map.json not on runtime classpath** → LuaResourceSupplier
   (ResourceSupplier SPI in :processor) embeds it at build time.
5. **TeaVM Long_fromNumber throws on Infinity/NaN** (BigInt of non-integer) —
   luaj LuaDouble.tojstring cast. Fixed luaj tojstring (NaN/Inf checks first)
   AND make_webapp.py patches the runtime helper to clamp (JVM semantics).
6. **TeaVM TDeflater/TInflater throw on Z_BUF_ERROR** (Badges.saveGlobal gzip)
   — JVM returns 0. Fixed in the local classlib fork (republish:
   `cd teavm && ./gradlew :classlib:publishToMavenLocal
   -Pteavm.project.version=0.13.1`, then rebuild with --refresh-dependencies).
7. **Reflection callables don't box primitives** — `list:size()` returned a
   raw JS number → luaj CoerceJavaToLua crashed ($hashCode of undefined).
   ClassGenerator.renderCallable now boxes (boxIfNecessary).
8. **Constructor.newInstance didn't run <clinit>** — initClass in
   ClassGenerator now emits class-init for <init> too (was needed for Wand
   statics; note Wand.handler null at title time is DESKTOP behavior too —
   ctor catches).
9. **JDK classes returned to lua had no metadata** — supplier now exposes an
   explicit EXTRA_EXPOSED allowlist (collections + java.lang basics +
   org.json). Keep it tight: broad exposure (guava/java.io/java.lang.Class)
   OOMs the build or hits unlinked classlib APIs.
10. **WebGL has no client-side vertex arrays** — black screen, every
    drawElements GL_INVALID_OPERATION. html NoosaScript/MaskedTilemapScript
    now upload quad data to VBOs (GlBuffers helper) and draw with byte
    offsets; Attribute got an int-offset vertexPointer overload.

## Debug tooling added

- make_webapp.py patches teavm-app.js at assembly: Long_fromNumber clamp +
  `$rt_wrapException` ring buffer `window.__jsErrLog` (real JS stacks for
  errors crossing into Java — TeaVM's own getStackTrace is always empty).
- index.html hides the boot overlay once rAF ticks (>5), and reads
  `window.__rafTicks`.
- Browser loop: reload → evaluate `window.__errors` (console.error capture),
  `window.__jsErrLog`. Error count stable = boot OK.

## Remaining / next steps

1. **Input**: canvas click on title buttons did nothing yet — pointer/touch
   event plumbing from TeaApplication to Game.input needs a pass.
2. Title screen visual check vs desktop (possible scale/layout offsets:
   camera is 800x480 here, 480x320 on desktop).
3. Text/font rendering not verified (no visible text on title yet).
4. New game → dungeon generation → gameplay loop.
5. Saves (HtmlPreferences works? localStorage), sound (stubs), mods
   (listResources empty).
6. Remove debug breadcrumbs (jsErrLog hook ok to keep, cheap) and build with
   `-Pteavm.obfuscated=true` before shipping.

## Reference port

**github.com/glassesmonkey/shattered-pixel-dungeon-web** — Shattered Pixel
Dungeon playable in the browser, built exactly like this port (TeaVM + the
libgdx TeaVM backend, JS not WASM, static webapp). Worth mining before
solving anything hard ourselves:

- "WebGL-safe vertex-buffer paths for dynamic text, effects, tilemap updates,
  and partial VBO uploads" in `SPD-classes`
  (`com/watabou/glwrap/Vertexbuffer.java`) and the `noosa` layer — same
  client-arrays→VBO conversion as our GlBuffers/NoosaScript fix, but done
  inside the watabou glwrap plumbing (may be the cleaner place for
  Text/Emitter/Tilemap draw paths we haven't hit yet).
- Browser font and file handling shims for TeaVM (our Text rendering is
  unverified — look here first when it breaks).
- Save/reload logic reworked for browser lifecycle (pagehide/visibility) —
  relevant for our HtmlPreferences/localStorage work.

## How to debug this port (hard-won notes)

- **Boot hooks**: `make_webapp.py` embeds an inline script BEFORE
  `teavm-app.js` capturing `window.__errors` (console.error + window error
  events) and `window.__logs` (console.log/warn). System.err reaches the
  browser console (console.error); System.out does not.
- **`$jsException`**: TeaVM stores the raw JS error on wrapped Java
  Throwables — the jsErrLog hook records those stacks at the crossing point.
- **CAUSE-JSSTACK positions** map to `teavm-app.js` only in the
  unobfuscated build (`obfuscated = false` is the current default in
  RemixedDungeonHtml/build.gradle via `-Pteavm.obfuscated`).
- **Delombok is stale-prone**: main-source edits land in
  `RemixedDungeonHtml/build/delomboked/` — if output looks old, check there.
  Its "package does not exist" errors are pre-existing noise
  (ignoreExitValue = true).
- **hjson is unusable on TeaVM** (its regexes use `\x{...}` escapes) —
  `Util.sanitizeJson` now tries strict JSON first, hjson as fallback.
- **`BOMInputStream.close()` throws on TeaVM** even after a successful read —
  `JsonHelper.readJsonFromStream` treats a close failure with content in hand
  as non-fatal; keep that structure. The "Stream is closed" boot log line is
  this and is benign.
- **TeaVM reflection** (enableRef=true): `Class.forName` finds only classes
  TeaVM linked. `LuaReflectionSupplier` (in :processor,
  META-INF/services/org.teavm.classlib.ReflectionSupplier) declares all
  `lua-interface-map.json` classes via `getClassesFoundByName` and exposes
  their members. If new lua-bound classes appear, annotate them with
  `@LuaInterface` (method-level annotation puts the class in the map).
- **Classes that reference `java.text.*` / `java.util.logging.*`** must stay
  out of reflection metadata AND out of reachable-from-lua code — TeaVM
  cannot link those packages. App code was moved to
  String.CASE_INSENSITIVE_ORDER / GLog+FileWriter.
- **Mixed versions**: gradle plugin is upstream 0.13.1 (teavm.org), classlib
  is the local fork (mavenLocal, 0.13.1 — republish after fork edits, then
  build with --refresh-dependencies). `js.obfuscated` property from the CLI
  (`-Pjs.obfuscated=false`) does NOT work; use the DSL `obfuscated = ...` in
  build.gradle (done).
- The `teavm` and `luaj` git submodules are pinned and clean; luaj had
  `os.execute` stripped (JseProcess excluded from jar, JseOsLib returns
  EXEC_ERROR). Both submodules now carry local commits (see git log) —
  commit inside the submodule first, then bump the pin here.

## Tooling

- `RemixedDungeonHtml/make_webapp.py` — assembles `build/webapp`: teavm-app.js
  (+ runtime patches), index.html (hooks + main() call), assets/ +
  assets.txt manifest (type:kind:path:length:overwrite), `scripts/gdx.wasm.js`
  + `assets/startup-logo.png` extracted from backend jar, gdx classpath
  resources. `--skip-build` reuses the last generateJavaScript output.
- `RemixedDungeonHtml/serve.py --port 8081` — no-store static server.
- Browser test loop: reload → evaluate `window.__errors` / `window.__jsErrLog`.
- Three-platform compile check:
  - html: `./gradlew -c settings.html.gradle :RemixedDungeonHtml:generateJavaScript`
  - desktop: `./gradlew :RemixedDungeonDesktop:compileJava`
  - android: `./gradlew -c settings.android.gradle :RemixedDungeon:compileAndroidFdroidDebugJavaWithJavac`

## Open questions / later

- Sound: html audio shims are stubs; `isSoundExists` returns true blindly.
  Low priority per goal.
- Mods on web: `listResources` returns empty; mod loading unexplored.
- `index.html` in `RemixedDungeonHtml/src/main/webapp/` is stale GWT-era junk
  (the real one is generated by make_webapp.py).
- Consider upstream TeaVM master (2f378217c) — has Integer.sum etc.; we fixed
  app-side instead. Rebasing the fork's stubs is possible but risky.
