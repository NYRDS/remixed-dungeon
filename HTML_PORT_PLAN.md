# HTML (TeaVM) port — resume plan

Branch: `html-port-runnable` (work in progress, see git log)
Serving setup: `python3 RemixedDungeonHtml/make_webapp.py --skip-build` then
`python3 RemixedDungeonHtml/serve.py --port 8081` → http://127.0.0.1:8081

## Current state (as of 2026-09-06, session 6)

- **Debug entrypoints**: `?ep=newgame[&hero=WARRIOR&difficulty=2]` boots
  straight into a fresh dungeon (title still builds first, intro skipped).
  GameScene reached and rendered; verified against desktop via its
  `/debug/screenshot` endpoint.
- **The "stuck story window / frozen loop" family is fixed** (three stacked
  causes): (1) TeaApplication pauses on visibilitychange-hidden and the
  matching resume was missed after occlusion → permanent pause; html
  Game.render now self-heals when the document is visible again. (2) The rAF
  shim armed a 250ms timer per frame unconditionally → Chromium timer-budget
  throttling froze visible pages; fallbacks now arm only while rAF looks
  dead, and a web-worker ticker (worker timers survive occlusion) fires
  frames pending >600ms. (3) DebugEntryPoints originally reposted itself via
  pushUiTask *inside the uiTask drain* → infinite drain spin; reposts go
  through Gdx.app.postRunnable (copied-then-drained per step).
- **Browser telemetry** (read via console/evaluate): `window.__gameState`
  (scene name, frame heartbeat, renderDone), `__rafShimStats`
  (viaRaf/viaFallback/viaWorker/err), `__workerTicks`, `__epPoll`,
  `__kickLoop()` (re-invokes the last frame callback), `__lastSuspendStack`
  ($rt_suspending tracer), `__suspends` (TeaVMThread tracer). The canvas
  capture (`__frameData`) now lives in the shim itself.
- **GameScene defect review vs desktop**: black void right of the town map
  also exists on desktop — NOT a bug. Toolbar-top vs html toolbar-bottom is
  branch age (html branch is 32.3.alpha code, which anchors the toolbar
  bottom; desktop master moved it up) — NOT a bug. REAL defect: StatusPane
  top-bar renders wrong texture regions on web (glyph-atlas rows around the
  portrait, pink menu button, missing mana bar) — bd snap-4o4.
- **Desktop debug server recipe**: `-Pargs` does NOT propagate to
  runDesktopGameWithWebServer (game boots fullscreen, no server). Launch
  java directly from `RemixedDungeonDesktop/src/desktop/rundir` with the
  runtime classpath (see bd snap-fqj notes) plus `--windowed
  --webserver=8090`, then `curl /debug/start_game?class=WARRIOR&difficulty=2`
  and `/debug/screenshot`. System.out.println is INVISIBLE on web — use
  PUtil.slog (gdx Logger → console.error) for web-visible markers.

## Current state (as of 2026-09-06, session 5)

- **THE GAME IS PLAYABLE END TO END**: boot → title → hero select →
  difficulty → intro → dungeon generation → GameScene renders (tilemap, hero,
  mobs, UI) and click-to-move works. Verified live in the browser with canvas
  captures.
- Blocked-to-playable fixes this session (in discovery order):
  1. **TeaInput click coordinates** (gdx-teavm 1.3.0 bug): the
     `getSubPixelAbsoluteLeft/Top` offset walk advances the wrong variable,
     dropping the parent-element offset (our centered #game-container), so
     every click landed shifted by (240,20) and hit the wrong button.
     make_webapp.py rewrites the 4 `getRelativeX/Y` fns to use
     `getBoundingClientRect()` (scroll-correct, keeps CSS-scale ratio).
  2. **`ReportingExecutor.submit()` returned null** (html stub; TeaVM has no
     ThreadPoolExecutor/ExecutorService/FutureTask) → `GameLoop.stepExecute`
     future was null → InterlevelScene crashed on `levelChanger.isDone()`.
     Fixed with `SimpleFuture` (shared, TeaVM-safe: TeaVM's TFuture has no
     TimeoutException) + real submit() in the html executor.
  3. **Room.Type painter `getMethod("paint")` threw NoSuchMethodException**
     (TeaVM exposes reflective methods only for reflection-scope classes;
     painters aren't in it) → level gen died → "GameScene when level is nil".
     Replaced reflection with direct `Painter::paint` method refs (shared
     code, desktop-identical).
  4. **`Char.getHeroClass`/`HeroClass.getEntityKind` lacked @LuaInterface** —
     desktop luajava reflects any public method, TeaVM luajava only exposes
     annotated ones → `PlagueDoctor.lua:211 attempt to call a nil value`
     killed GameScene.create via GameLoop's LuaError→ModError rethrow.
- Diagnostics added (keep, cheap): `__reflFail` wrapper logs failed
  getMethod/getDeclaredConstructor/newInstance lookups with class+member;
  LevelChanger and GameLoop scene-switch now report exceptions via
  EventCollector (Java stack traces are empty on TeaVM — this is the only
  attribution path).
- **Occlusion shim**: rAF never fires in an occluded pane and TeaApplication
  never schedules frames again until visibility returns; index.html (via
  make_webapp.py) drives rAF callbacks from timers when
  `document.visibilityState === 'hidden'` (~0.25x game speed, stats in
  `__rafShimStats`). Visible mode untouched.
- Known cosmetic issues: top-left UI strip draws font-atlas glyph rows
  (wrong texture region in some top-bar element), right side of town level
  renders black beyond the map bounds (level 0 town is small; check whether
  desktop shows void the same way).

## Debug tooling added (session 5)

- In-page capture wrapper (install after boot): wraps rAF, stores
  `window.__frameData` via toDataURL after each frame.
- Click geometry: canvas css offset (fresh `getBoundingClientRect()` per
  session) + css→cua scale (probe with a click + mousedown listener; was
  1.3315 then 4/3 across pane resizes). gameX = clientX - rect.left.
- `window.__reflFail`, `window.__rafShimStats`, `__jsErrLog` (50-entry ring),
  `__errors` (console.error capture; ~350 entries per boot is the baseline).

## Current state (as of 2026-09-06, session 4)

- **TEXT RENDERS** — the session-3 blocker is gone. Title button labels,
  version string, StartScene (hero names, Load/New Game, subtitles), wrapped
  multiline colored hint text and the WndClass info window (bulleted
  paragraphs) all render correctly with the pixel font. Verified in-browser
  via in-page canvas captures.
- **INPUT STILL WORKS**: clicks drive Title ⇄ StartScene switches (verified
  live again this session).
- Boot is healthy: strings parse, lua sandbox initializes, error count stable
  at boot-time-only entries, ~58fps.
- Compile ✅ html/desktop/android. teavm-app.js ~40MB debug (obfuscated=false).

## What was fixed this session (text rendering, in boot order)

0. **gdx version skew** — gradle resolved gdx 1.12.1 → **1.13.5** (gdx-teavm
   1.3.0's transitive wins). The module now declares 1.13.5 explicitly; 1.12.1
   silently linked 1.13.5 classes all along.
1. **FreeType on TeaVM** — html SystemText was a measure-only stub (never
   drew). Rewrote it as a copy of the desktop FreeType implementation
   (FreeTypeFontGenerator + PseudoGlyphLayout/PseudoPixmapPacker +
   SystemTextPseudoBatch → NoosaScript VBO quads, oversample 4, markup, wrap).
   Deps: `com.badlogicgames.gdx:gdx-freetype` + `com.github.xpenatan.gdx-teavm:
   gdx-freetype-teavm` (its emu FreeType replaces the JNI class via the
   mapPackageHierarchy plugin, already active from backend-teavm). The 18MB
   CJK fallback font is deliberately NOT shipped — fallback generator is
   optional, CJK falls back to the pixel font for now.
2. **freetype.js deployment** — the emscripten freetype Module ships as a jar
   resource; make_webapp.py extracts it to `scripts/freetype.js` and
   TeaVMLauncher's `config.preloadListener` loads it before the app starts.
3. **i18n bundles were never loaded** — the html StringsManager was a stub
   whose maps stayed empty (every getVar returned ""!). Ported the desktop
   implementation (line-based JSON parsing, format-regex, allChars). l10ns
   (`RemixedDungeonHtml/l10ns/strings_*.json`, 22 langs) are copied into the
   webapp + manifest by make_webapp.py.
4. **R name→id mapping without reflection** — make_r.py now emits
   `R.names[]` (index == id); StringsManager builds keyToInt from it.
5. **R$string/R$array for TeaVM reflection** — Utils.getR_Field does
   Class.forName("...R$string") in clinit; added both to
   LuaReflectionSupplier's REFLECT_EXTRAS (findable by name + exposed
   fields) so Class.forName/getField/getInt work.
6. **GlyphRun pool crashed TeaVM** — `Pools.get(GlyphRun.class)` uses
   reflective instantiation ("missing no-arg constructor"). TeaVMLauncher now
   registers a direct-alloc pool via `Pools.set` before anything touches
   GlyphLayout (covers stock GlyphLayout too).
7. **delombok REMOVED** (Mike: fix the noise) — lombok already runs as an
   annotationProcessor in compileJava, so TeaVM consumes expanded bytecode;
   the delomboked tree only duplicated sources and spewed unfixable "cannot
   find symbol" noise. Build output is clean now.
8. **EventCollector.setSessionData spam** — LuaScript calls it every tick;
   each print became a console.error and the flood starved browser DevTools
   and screenshot capture. Now silent.

## Debug tooling added

- In-page canvas capture (works around IAB screenshot timeouts): wrap
  window.requestAnimationFrame so that right after the game's frame callback
  returns, `canvas.toDataURL('image/png')` runs in the same task
  (preserveDrawingBuffer=false otherwise yields black). Poll
  `window.__frameData`, decode outside. Screenshot surface prep routinely
  times out on this page otherwise.

## Session 3 fixes (boot + input), for history

0. **Input plumbing (session 3)** — clicks on title buttons did nothing because
   the html `Touchscreen.processEvent` was a stub (`println` only); GameLoop
   polls it via `Touchscreen.processEvent(PointerEvent)`. Replaced with the
   desktop implementation verbatim (Signal dispatch of Touch, pointers map).
   Also wired html `Game.keyDown/keyUp` + render() auto-fire to enqueue
   `KeyEvent`s into `GameLoop.keysEvents` exactly like desktop (they only
   tracked keyDownTimes before). Verified END TO END in the browser:
   Title→Rankings (dashboard click) and Rankings→Title (exit button), correct
   coords with the page scrolled both axes. `System.setOut(System.err)` added
   in TeaVMLauncher — System.out never reaches the browser console; merging
   makes all game logs visible in the `__errors` capture.

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

1. **New game → dungeon generation → gameplay loop** (the next big rock;
   StartScene renders, so the path is: New Game click → level gen → game
   scene with tilemap/items/mobs).
2. RU/other-locale text: strings_ru.json loads, Cyrillic glyphs come from
   langNames in getAllCharsAsString — verify a switched locale renders.
3. CJK fallback font (LXGWWenKaiScreen.ttf, 18MB) — currently not shipped;
   CJK falls back to the pixel font (missing glyphs). Decide: ship lazily
   (fetch on demand) or accept missing CJK on web.
4. Title screen layout vs desktop: camera is 800x480 here, 480x320 on
   desktop. Re-check proportions now that text measures correctly.
5. Saves (HtmlPreferences works? localStorage; `save_io_exception` already
   logged at boot), sound (stubs), mods (listResources empty).
6. Keys flow now enqueues events but is unverified live (Escape/back).
7. Remove debug breadcrumbs (jsErrLog hook ok to keep, cheap; System.setOut
   merge worth keeping while porting) and build with
   `-Pteavm.obfuscated=true` before shipping.

## Browser-testing notes (in-app pane)

- The IAB pane's real css viewport ≠ setViewportSize; measure
  `getBoundingClientRect()` + `window.innerWidth/devicePixelRatio` per
  session. `tab.cua` clicks are css × dpr (observed 1.331); screenshots are
  in the same cua space. The pane can be smaller than the 800x480 canvas —
  scroll (`document.scrollingElement.scrollLeft/Top`) and TeaInput's
  scroll-compensating math handles it (verified).

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
- Browser font and file handling shims for TeaVM — RESOLVED for fonts: they
  use FreeTypeFontGenerator on TeaVM via gdx-freetype-teavm (same module we
  adopted; see session 4). Still worth mining for file-handling patterns.
- Save/reload logic reworked for browser lifecycle (pagehide/visibility) —
  relevant for our HtmlPreferences/localStorage work.

**github.com/ArcaneCircle/pixel-dungeon** ("Arcane Dungeon") — secondary
reference: classic Watabou Pixel Dungeon as a Webxdc app. NOT TeaVM — it's a
GWT (Java→JS) port with vite/pnpm tooling, shipped as a .xdc for Delta Chat
rather than a hosted site. Still worth mining for GWT-era solutions to the
same problems (canvas input mapping, asset packaging, saves in
localStorage). GPL-3.0 like upstream.

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
