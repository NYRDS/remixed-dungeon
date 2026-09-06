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
import shutil
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ASSETS_SRC = os.path.normpath(os.path.join(HERE, "..", "RemixedDungeon", "src", "main", "assets"))


def build_teavm_js() -> str:
    subprocess.check_call(
        ["./gradlew", "-c", "settings.html.gradle", ":RemixedDungeonHtml:generateJavaScript",
         "--console=plain"],
        cwd=os.path.normpath(os.path.join(HERE, "..")),
    )
    return os.path.join(HERE, "build", "generated", "teavm", "js", "teavm-app.js")


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
        // TeaVM exports the entry point as main() - plain script tags put it
        // on window, so start the game explicitly
        if (typeof main === "function") {
            main();
        } else {
            document.getElementById("loading").textContent =
                "teavm-app.js did not export main()";
        }
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


def extract_backend_resources(app_dir: str) -> None:
    """TeaApplication.initGdx() loads <page>/scripts/gdx.wasm.js and the
    preload screen wants assets/startup-logo.png - both are backend jar
    resources that TeaBuilder would normally copy into the webapp."""
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
        "~/.gradle/caches/modules-2/files-2.1/com.badlogicgames.gdx/gdx/1.12.1/*/gdx-1.12.1.jar"))
    if not jars:
        raise SystemExit("gdx jar not found in gradle cache")
    with zipfile.ZipFile(sorted(jars)[-1]) as z:
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

    shutil.copy2(js_src, os.path.join(app_dir, "teavm-app.js"))
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

    with open(os.path.join(assets_dir, "assets.txt"), "w") as f:
        f.write("\n".join(lines) + "\n")

    print("webapp ready: %s (%d assets, %.1f MB)" % (
        app_dir, count, sum(os.path.getsize(os.path.join(dp, fn))
                            for dp, _dn, fns in os.walk(assets_dir) for fn in fns) / 1e6))


if __name__ == "__main__":
    sys.exit(main())
