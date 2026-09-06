#!/usr/bin/env python3
"""Assemble a servable webapp dir for the TeaVM html build.

Layout expected by the gdx-teavm backend (AssetLoadImpl):
  <webroot>/index.html        page hosting the canvas
  <webroot>/teavm-app.js      TeaVM output (generateJavaScript task)
  <webroot>/assets/assets.txt preload manifest: type:kind:path:length:overwrite
  <webroot>/assets/<...>      game asset tree (RemixedDungeon/src/main/assets)

Usage: python3 make_webapp.py [--app-dir build/webapp]
After it, run serve.py --dir build/webapp and open http://localhost:8081
"""
import argparse
import os
import re
import shutil
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ASSETS_SRC = os.path.normpath(os.path.join(HERE, "..", "RemixedDungeon", "src", "main", "assets"))
# line-based JSON i18n bundles read by StringsManager.useLocale via
# ModdingMode.getInputStream("strings_<lang>.json")
L10NS_SRC = os.path.join(HERE, "l10ns")


def build_teavm_js() -> str:
    subprocess.check_call(
        ["./gradlew", "-c", "settings.html.gradle", ":RemixedDungeonHtml:generateJavaScript",
         "--console=plain"],
        cwd=os.path.normpath(os.path.join(HERE, "..")),
    )
    return os.path.join(HERE, "build", "generated", "teavm", "js", "teavm-app.js")


# TeaVM's Long_fromNumber (double -> long cast) throws RangeError on Infinity/NaN
# (BigInt of a non-integer), while JVM semantics clamp to Long.MAX/MIN and map
# NaN to 0 - luaj's LuaDouble.tojstring hits this on tostring(inf). Patch the
# runtime helper until a local teavm-core build can replace the upstream jar.
LONG_FROM_NUMBER_SRC = (
    "Long_fromNumber = val => BigInt.asIntN(64, "
    "BigInt(val >= 0 ? Math.floor(val) : Math.ceil(val)))"
)
LONG_FROM_NUMBER_DST = (
    "Long_fromNumber = val => val !== val ? BigInt(0)"
    " : (val >= 9223372036854775808 ? BigInt('9223372036854775807')"
    " : (val < -9223372036854775808 ? BigInt('-9223372036854775808')"
    " : BigInt.asIntN(64, BigInt(val >= 0 ? Math.floor(val) : Math.ceil(val)))))"
)


def patch_teavm_js(js_text: str) -> str:
    if LONG_FROM_NUMBER_SRC in js_text:
        js_text = js_text.replace(LONG_FROM_NUMBER_SRC, LONG_FROM_NUMBER_DST)
    else:
        print("warning: Long_fromNumber pattern not found - runtime patch skipped")
    js_text = patch_tea_input_coords(js_text)
    js_text = patch_reflection_failure_log(js_text)
    # keep a ring buffer of JS errors crossing into Java, with real JS stacks
    # (TeaVM's getStackTrace is always empty, so this is the only way to see
    # where a (JavaScript) TypeError actually came from)
    wrap_src = "$rt_wrapException = err => {\n    let ex = err[$rt_javaExceptionProp];"
    wrap_dst = (
        "$rt_wrapException = err => {\n"
        "    if (typeof window !== 'undefined') {\n"
        "        let rb = (window.__jsErrLog = window.__jsErrLog || []);\n"
        "        if (rb.length < 50) rb.push({msg: String(err && err.message || err),\n"
        "            stack: String(err && err.stack || '')});\n"
        "    }\n"
        "    let ex = err[$rt_javaExceptionProp];"
    )
    if wrap_src in js_text:
        js_text = js_text.replace(wrap_src, wrap_dst)
    else:
        print("warning: $rt_wrapException pattern not found - error log hook skipped")
    return js_text


# Java-side exceptions on TeaVM carry no stack frames, so a bare
# NoSuchMethodException (e.g. from a failed getMethod/getDeclaredConstructor
# during level generation) is otherwise unattributable. Wrap TeaVM's
# reflective lookup entry points to record which class/member failed.
REFL_HOOK_ANCHOR = "$rt_exports.main = $rt_export_main;"
REFL_HOOK_CODE = """{
const __reflWrap = (orig, label, withName) => (...a) => {
    try { return orig(...a); } catch (e) {
        try {
            let m = label + "(" + $rt_ustr(jl_Class_getName(a[0]));
            if (withName && typeof a[1] === "object" && a[1] !== null) m += ", " + $rt_ustr(a[1]);
            m += ") -> " + e;
            (window.__reflFail = window.__reflFail || []).push(m);
        } catch (e2) {}
        throw e;
    }
};
const __reflWrapIf = (name, label, withName) => {
    // some of these are dead-code-eliminated by TeaVM; ReferenceError means skip
    try {
        const cur = eval(name);
        if (typeof cur === "function") eval(name + " = __reflWrap(cur, label, withName)");
    } catch (e) {}
};
__reflWrapIf("jl_Class_getMethod", "getMethod", true);
__reflWrapIf("jl_Class_getDeclaredMethod", "getDeclaredMethod", true);
__reflWrapIf("jl_Class_getConstructor", "getConstructor", false);
__reflWrapIf("jl_Class_getDeclaredConstructor", "getDeclaredConstructor", false);
__reflWrapIf("jl_Class_newInstance", "newInstance", false);
}
"""


def patch_reflection_failure_log(js_text: str) -> str:
    if REFL_HOOK_ANCHOR not in js_text:
        print("warning: reflection hook anchor not found - no reflective failure logging")
        return js_text
    js_text = js_text.replace(REFL_HOOK_ANCHOR, REFL_HOOK_CODE + REFL_HOOK_ANCHOR, 1)
    print("reflection failure logging installed (5 entry points wrapped)")
    return js_text


# xpenatan gdx-teavm 1.3.0 TeaInput.getSubPixelAbsoluteLeft/Top advances its
# second loop with the wrong local (elem = curr.getOffsetParent() instead of
# elem.getOffsetParent()), so any canvas offset coming from a parent element
# (e.g. our centered #game-container) is dropped and every click lands
# shifted by that offset. Replace the clientXY -> canvasXY conversion with
# getBoundingClientRect(), which is viewport-relative (ancestor scroll
# already included) and pairs directly with clientX/clientY; keep the
# canvas-pixel / CSS-client ratio so CSS scaling still maps correctly.
TEA_INPUT_COORD_FUNCS = {
    # generated fn name: (event param, client prop, rect prop, canvas size prop, client size fn)
    "getRelativeX0": ("$e", "clientX", "left", "width", "getClientWidth"),
    "getRelativeY0": ("$e", "clientY", "top", "height", "getClientHeight"),
    "getRelativeX": ("$touch", "clientX", "left", "width", "getClientWidth"),
    "getRelativeY": ("$touch", "clientY", "top", "height", "getClientHeight"),
}


def patch_tea_input_coords(js_text: str) -> str:
    patched = 0
    for fn, (ev, client, rect, size, dim_fn) in TEA_INPUT_COORD_FUNCS.items():
        pattern = re.compile(
            r"([\w$]+)_TeaInput_" + fn
            + r" = \(\$this, " + re.escape(ev) + r", \$target\) => \{\n"
            r"    return [^\n]*\n\},")
        def repl(m, fn=fn, ev=ev, client=client, rect=rect, size=size, dim_fn=dim_fn):
            p = m.group(1)
            return (f"{p}_TeaInput_{fn} = ($this, {ev}, $target) => {{\n"
                    f"    return jl_Math_round($target.{size} * 1.0"
                    f" / {p}_TeaInput_{dim_fn}($this, $target)"
                    f" * ({ev}.{client} - $target.getBoundingClientRect().{rect}));\n}},")
        js_text, n = pattern.subn(repl, js_text)
        patched += n
    if patched < len(TEA_INPUT_COORD_FUNCS):
        print(f"warning: TeaInput coord patch applied to {patched}/"
              f"{len(TEA_INPUT_COORD_FUNCS)} functions")
    else:
        print("TeaInput coord patch: 4 functions rewritten (getBoundingClientRect)")
    return js_text


INDEX_HTML = """<!DOCTYPE html>
<html>
<head>
    <title>Remixed Dungeon</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <style>
        body { margin: 0; padding: 0; background: #000; color: #fff;
               font-family: Arial, sans-serif; }
        #game-container { position: relative; width: 800px; height: 480px;
                          margin: 20px auto; }
        #canvas { width: 100%; height: 100%; }
        #loading { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
                   font-size: 24px; }
    </style>
</head>
<body>
    <script>
        // rAF never fires in an occluded pane/tab, which stalls the whole game.
        // When hidden, drive the TeaApplication frame callback from timers
        // instead (throttled to ~1/s by the browser; GameLoop clamps dt to
        // 250ms, so game time advances at ~0.25x). Visible mode is untouched.
        (function() {
            var origRAF = window.requestAnimationFrame.bind(window);
            var synthT = 0;
            window.__rafShimStats = { sched: 0, fired: 0, err: 0 };
            window.requestAnimationFrame = function(cb) {
                if (document.visibilityState !== 'hidden') return origRAF(cb);
                window.__rafShimStats.sched++;
                setTimeout(function() {
                    window.__rafShimStats.fired++;
                    if (document.visibilityState !== 'hidden') { origRAF(cb); return; }
                    synthT += 250;
                    try {
                        cb(synthT);
                    } catch (e) {
                        window.__rafShimStats.err++;
                        window.__errors.push('shim cb error: ' + e);
                        return;
                    }
                    try {
                        window.__frameData =
                            document.getElementById('canvas').toDataURL('image/png');
                    } catch (e) {}
                }, 30);
                return 0;
            };
        })();
        // boot error capture - must run before teavm-app.js
        window.__errors = [];
        window.__logs = [];
        window.addEventListener('error', function(e) {
            var stack = (e.error && e.error.stack) ? String(e.error.stack) : '';
            window.__errors.push(String(e.message) + ' @ ' + String(e.filename || '?') + ':' + e.lineno
                + (stack ? ' STACK: ' + stack.substring(0, 2000) : ''));
            if (e.error) {
                window.__lastError = e.error;
                try {
                    window.__lastErrorProps = {};
                    var o = e.error;
                    do {
                        Object.getOwnPropertyNames(o).forEach(function(k) {
                            if (!(k in window.__lastErrorProps)) {
                                var v;
                                try { v = String(o[k]); } catch (err) { v = '<unavailable>'; }
                                window.__lastErrorProps[k] = v.substring(0, 300);
                            }
                        });
                        o = Object.getPrototypeOf(o);
                    } while (o && o !== Object.prototype);
                } catch (err) {}
            }
        });
        window.addEventListener('unhandledrejection', function(e) {
            window.__errors.push('rejection: ' + String(e.reason));
        });
        (function() {
            var origLog = console.log.bind(console);
            console.log = function() {
                var a = Array.prototype.slice.call(arguments);
                if (window.__logs.length < 500) window.__logs.push(a.map(String).join(' '));
                origLog.apply(null, a);
            };
            var origErr = console.error.bind(console);
            console.error = function() {
                var a = Array.prototype.slice.call(arguments);
                window.__errors.push(a.map(String).join(' '));
                origErr.apply(null, a);
            };
            var origWarn = console.warn.bind(console);
            console.warn = function() {
                var a = Array.prototype.slice.call(arguments);
                if (window.__logs.length < 500) window.__logs.push('WARN: ' + a.map(String).join(' '));
                origWarn.apply(null, a);
            };
        })();
    </script>
    <div id="game-container">
        <div id="loading">Loading Remixed Dungeon...</div>
        <canvas id="canvas" tabindex="1"></canvas>
    </div>
    <script type="text/javascript" src="teavm-app.js"></script>
    <script>
        // count render-loop ticks so the boot overlay can retire itself once
        // the game is actually drawing
        (function() {
            var ticks = 0;
            var orig = window.requestAnimationFrame;
            if (!orig) { return; }
            window.requestAnimationFrame = function(cb) {
                ticks++;
                window.__rafTicks = ticks;
                return orig.call(window, cb);
            };
        })();
        // TeaVM exports the entry point as main() - plain script tags put it
        // on window, so start the game explicitly
        if (typeof main === "function") {
            main();
        } else {
            document.getElementById("loading").textContent =
                "teavm-app.js did not export main()";
        }
        // the game never touches this div; once the render loop is ticking it
        // only covers the canvas, so drop it
        (function() {
            var tries = 0;
            var timer = setInterval(function() {
                tries++;
                if (window.__rafTicks > 5 || tries > 30) {
                    clearInterval(timer);
                    var el = document.getElementById("loading");
                    if (el) { el.style.display = "none"; }
                }
            }, 1000);
        })();
    </script>
</body>
</html>
"""


def find_backend_jar() -> str:
    """Locate the gdx-teavm backend jar in the gradle cache."""
    import glob
    hits = glob.glob(os.path.expanduser(
        "~/.gradle/caches/modules-2/files-2.1/com.github.xpenatan.gdx-teavm/"
        "backend-teavm/*/*/backend-teavm-*.jar"))
    hits = [h for h in hits if not h.endswith("sources.jar")]
    if not hits:
        raise SystemExit("backend-teavm jar not found in gradle cache")
    return sorted(hits)[-1]


def find_freetype_jar() -> str:
    """Locate the gdx-teavm freetype jar (ships the emscripten freetype.js)."""
    import glob
    hits = glob.glob(os.path.expanduser(
        "~/.gradle/caches/modules-2/files-2.1/com.github.xpenatan.gdx-teavm/"
        "gdx-freetype-teavm/*/*/gdx-freetype-teavm-*.jar"))
    hits = [h for h in hits if not h.endswith("sources.jar")]
    if not hits:
        raise SystemExit("gdx-freetype-teavm jar not found in gradle cache")
    return sorted(hits)[-1]


def extract_backend_resources(app_dir: str) -> None:
    """TeaApplication.initGdx() loads <page>/scripts/gdx.wasm.js and the
    preload screen wants assets/startup-logo.png - both are backend jar
    resources that TeaBuilder would normally copy into the webapp.
    freetype.js (emscripten freetype Module) comes from gdx-freetype-teavm;
    TeaVMLauncher's preload listener loads <page>/scripts/freetype.js before
    the game starts, so FreeTypeFontGenerator works."""
    import zipfile
    jar = find_backend_jar()
    os.makedirs(os.path.join(app_dir, "scripts"), exist_ok=True)
    with zipfile.ZipFile(jar) as z:
        with z.open("gdx.wasm.js") as src, \
                open(os.path.join(app_dir, "scripts", "gdx.wasm.js"), "wb") as dst:
            shutil.copyfileobj(src, dst)
        with z.open("startup-logo.png") as src, \
                open(os.path.join(app_dir, "assets", "startup-logo.png"), "wb") as dst:
            shutil.copyfileobj(src, dst)
    with zipfile.ZipFile(find_freetype_jar()) as z:
        with z.open("freetype.js") as src, \
                open(os.path.join(app_dir, "scripts", "freetype.js"), "wb") as dst:
            shutil.copyfileobj(src, dst)


# gdx classpath resources the game reads via Gdx.files.classpath (e.g. the
# default BitmapFont). On web the Classpath file storage is populated only
# from 'c:' entries in assets.txt, so ship them alongside the game assets.
CLASSPATH_RESOURCES = [
    "com/badlogic/gdx/utils/lsans-15.fnt",
    "com/badlogic/gdx/utils/lsans-15.png",
]


def extract_classpath_resources(assets_dir: str, lines: list) -> None:
    import glob
    import zipfile
    jars = glob.glob(os.path.expanduser(
        "~/.gradle/caches/modules-2/files-2.1/com.badlogicgames.gdx/gdx/*/*/gdx-*.jar"))
    # only the core gdx jar, not -sources and not sibling artifacts
    jars = [j for j in jars if "/gdx/" in j and not j.endswith("sources.jar")]
    if not jars:
        raise SystemExit("gdx jar not found in gradle cache")
    jars.sort()
    with zipfile.ZipFile(jars[-1]) as z:
        for res in CLASSPATH_RESOURCES:
            target = os.path.join(assets_dir, res)
            os.makedirs(os.path.dirname(target), exist_ok=True)
            with z.open(res) as src, open(target, "wb") as dst:
                shutil.copyfileobj(src, dst)
            lines.append("c:b:%s:%d:0" % (res, os.path.getsize(target)))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--app-dir", default=os.path.join(HERE, "build", "webapp"))
    parser.add_argument("--skip-build", action="store_true",
                        help="reuse build/generated/teavm/js/teavm-app.js as-is")
    args = parser.parse_args()

    app_dir = os.path.abspath(args.app_dir)
    assets_dir = os.path.join(app_dir, "assets")

    if os.path.isdir(assets_dir):
        shutil.rmtree(assets_dir)
    os.makedirs(assets_dir)

    js_src = os.path.join(HERE, "build", "generated", "teavm", "js", "teavm-app.js")
    if args.skip_build or not os.path.exists(js_src):
        if not os.path.exists(js_src):
            js_src = build_teavm_js()
    if not args.skip_build and os.environ.get("MAKE_WEBAPP_BUILD") == "1":
        js_src = build_teavm_js()

    with open(js_src, "r", encoding="utf-8") as f:
        js_text = f.read()
    js_text = patch_teavm_js(js_text)
    with open(os.path.join(app_dir, "teavm-app.js"), "w", encoding="utf-8") as f:
        f.write(js_text)
    for extra in ("teavm-app.js.map", "teavm-app.js.teavmdbg"):
        side = os.path.join(os.path.dirname(js_src), extra)
        if os.path.exists(side):
            shutil.copy2(side, os.path.join(app_dir, extra))

    with open(os.path.join(app_dir, "index.html"), "w") as f:
        f.write(INDEX_HTML)

    extract_backend_resources(app_dir)

    lines = []
    count = 0
    # followlinks: assets/scripts is a symlink to the repo-root scripts/ tree
    for root, _dirs, files in os.walk(ASSETS_SRC, followlinks=True):
        for name in files:
            src = os.path.join(root, name)
            rel = os.path.relpath(src, ASSETS_SRC).replace(os.sep, "/")
            dst = os.path.join(assets_dir, rel)
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy2(src, dst)
            size = os.path.getsize(src)
            # i=Internal, b=Binary, no local overwrite
            lines.append("i:b:%s:%d:0" % (rel, size))
            count += 1

    extract_classpath_resources(assets_dir, lines)

    # i18n bundles - StringsManager parses strings_<lang>.json at boot
    for name in sorted(os.listdir(L10NS_SRC)):
        if not name.endswith(".json"):
            continue
        src = os.path.join(L10NS_SRC, name)
        shutil.copy2(src, os.path.join(assets_dir, name))
        lines.append("i:b:%s:%d:0" % (name, os.path.getsize(src)))
        count += 1

    with open(os.path.join(assets_dir, "assets.txt"), "w") as f:
        f.write("\n".join(lines) + "\n")

    print("webapp ready: %s (%d assets, %.1f MB)" % (
        app_dir, count, sum(os.path.getsize(os.path.join(dp, fn))
                            for dp, _dn, fns in os.walk(assets_dir) for fn in fns) / 1e6))


if __name__ == "__main__":
    sys.exit(main())
