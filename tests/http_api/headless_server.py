"""Shared game-server launch for the HTTP API tests.

Set RPD_HEADLESS_JAR (path to RemixedDungeonHeadless-all.jar) to run the
no-GL headless build - the only mode that works on CI (no display; the
desktop GUI run also freezes forever under --minimized). The headless
process needs a rundir-style cwd whose parent exposes assets/, d_assets/
and l10ns/ - point RPD_GAME_DIR at it (a throwaway copy keeps test saves
out of the developer's rundir).

Without the env var the tests fall back to the legacy desktop GUI gradle
run for local interactive debugging.
"""

import os


def server_command(port: str = "8080"):
    """Return (cmd, cwd) to launch the game server for tests."""
    project_root = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    jar = os.environ.get("RPD_HEADLESS_JAR")
    if jar:
        game_dir = os.environ.get(
            "RPD_GAME_DIR", os.path.join(project_root, "RemixedDungeonDesktop/src/desktop/rundir"))
        return (["java", "-jar", os.path.abspath(jar), "--webserver=" + port],
                os.path.abspath(game_dir))
    return (["./gradlew", "-p", "RemixedDungeonDesktop", "runDesktopGameWithWebServer",
             "--args=--webserver=" + port + " --minimized"], project_root)


def emit_ci_error(log_file, tail: int = 25):
    """Surface the game log tail as ::error:: workflow commands - CI
    annotations are the only failure output readable through the API
    without credentials."""
    try:
        with open(log_file, "r", errors="replace") as f:
            lines = f.read().splitlines()[-tail:]
    except OSError:
        lines = ["<game log %s unreadable>" % log_file]
    for line in lines:
        print("::error::" + line[:250], flush=True)
