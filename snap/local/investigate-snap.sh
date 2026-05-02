#!/bin/bash
# Snap diagnostic script for remixed-dungeon
# Run: snap run --shell remixed-dungeon -c "bash /path/to/investigate-snap.sh"
# Or copy into snap and run from there.

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PASS=0
FAIL=0
WARN=0

pass() { ((PASS++)); echo -e "  ${GREEN}PASS${NC} $1"; }
fail() { ((FAIL++)); echo -e "  ${RED}FAIL${NC} $1"; }
warn() { ((WARN++)); echo -e "  ${YELLOW}WARN${NC} $1"; }
header() { echo -e "\n=== $1 ==="; }

# Determine snap root
SNAP_ROOT="${SNAP:-/snap/remixed-dungeon/current}"

header "1. Basic Snap Environment"
[ -n "$SNAP" ] && pass "SNAP=$SNAP" || warn "SNAP not set (running outside snap env)"
[ -n "$SNAP_USER_DATA" ] && pass "SNAP_USER_DATA=$SNAP_USER_DATA" || warn "SNAP_USER_DATA not set"
[ -n "$SNAP_NAME" ] && pass "SNAP_NAME=$SNAP_NAME" || warn "SNAP_NAME not set"
echo "  SNAP_ROOT=$SNAP_ROOT"

header "2. JDK Config Symlinks"
JVM_DIR="$SNAP_ROOT/usr/lib/jvm/java-17-openjdk-amd64"
if [ -d "$JVM_DIR/conf" ]; then
    pass "JVM conf dir exists: $JVM_DIR/conf"
    BROKEN=0
    TOTAL=0
    while IFS= read -r link; do
        ((TOTAL++))
        if [ ! -e "$link" ]; then
            ((BROKEN++))
            echo -e "    ${RED}BROKEN${NC} $(basename "$link") -> $(readlink "$link")"
        fi
    done < <(find "$JVM_DIR/conf" -type l 2>/dev/null)
    if [ "$BROKEN" -eq 0 ]; then
        pass "All $TOTAL conf symlinks resolve"
    else
        fail "$BROKEN/$TOTAL conf symlinks are broken"
    fi
else
    fail "JVM conf dir missing: $JVM_DIR/conf"
fi

header "3. JDK /etc/java-17-openjdk (layout bind-mount)"
ETC_JAVA="/etc/java-17-openjdk"
if [ -d "$ETC_JAVA" ]; then
    pass "$ETC_JAVA is accessible"
    for f in security security/java.security security/java.policy net.properties; do
        if [ -e "$ETC_JAVA/$f" ]; then
            if [ -f "$ETC_JAVA/$f" ]; then
                pass "  $ETC_JAVA/$f exists ($(wc -l < "$ETC_JAVA/$f" 2>/dev/null || echo '?') lines)"
            else
                pass "  $ETC_JAVA/$f exists (directory)"
            fi
        else
            fail "  $ETC_JAVA/$f MISSING"
        fi
    done
else
    fail "$ETC_JAVA not accessible (layout bind-mount may be broken)"
fi

header "4. JDK java binary"
JAVA_BIN="$SNAP_ROOT/usr/lib/jvm/java-17-openjdk-amd64/bin/java"
if [ -x "$JAVA_BIN" ]; then
    pass "java binary exists and is executable"
    JAVA_OUT=$("$JAVA_BIN" -version 2>&1)
    echo "  $JAVA_OUT"
else
    fail "java binary not found or not executable: $JAVA_BIN"
fi

header "5. Critical Libraries"
for lib in \
    "usr/lib/x86_64-linux-gnu/libGL.so.1" \
    "usr/lib/x86_64-linux-gnu/libGLdispatch.so.0" \
    "usr/lib/x86_64-linux-gnu/libGLX.so.0" \
    "usr/lib/x86_64-linux-gnu/libopenal.so.1" \
    "usr/lib/x86_64-linux-gnu/libasound.so.2" \
    "usr/lib/x86_64-linux-gnu/libgtk-3.so.0" \
    "usr/lib/x86_64-linux-gnu/libfreetype.so.6" \
    "usr/lib/x86_64-linux-gnu/libfontconfig.so.1" \
    "usr/lib/x86_64-linux-gnu/libudev.so.1" \
    "usr/lib/x86_64-linux-gnu/libsndfile.so.1" \
    "usr/lib/x86_64-linux-gnu/libX11.so.6" \
    "usr/lib/x86_64-linux-gnu/libX11-xcb.so.1" \
    "usr/lib/x86_64-linux-gnu/libglfw.so.3" \
; do
    FULL="$SNAP_ROOT/$lib"
    if [ -e "$FULL" ]; then
        if [ -L "$FULL" ] && [ ! -e "$FULL" ]; then
            fail "$lib -> broken symlink"
        else
            pass "$lib"
        fi
    else
        warn "$lib MISSING (may be provided by host)"
    fi
done
# Check DRI drivers (may be from host via opengl plug)
if [ -d "$SNAP_ROOT/usr/lib/x86_64-linux-gnu/dri" ]; then
    DRI_COUNT=$(ls "$SNAP_ROOT/usr/lib/x86_64-linux-gnu/dri/"*_dri.so 2>/dev/null | wc -l)
    if [ "$DRI_COUNT" -gt 0 ]; then
        pass "DRI drivers in snap: $DRI_COUNT files"
    else
        warn "DRI dir exists but empty (expecting host drivers via opengl plug)"
    fi
else
    warn "No DRI dir in snap (expecting host drivers via opengl plug)"
fi

header "6. xrandr"
XRANDR_BIN="$SNAP_ROOT/usr/bin/xrandr"
if [ -x "$XRANDR_BIN" ]; then
    pass "xrandr exists at $XRANDR_BIN"
else
    fail "xrandr not found at $XRANDR_BIN"
fi

header "7. Wrapper Script"
WRAPPER="$SNAP_ROOT/bin/remixed-dungeon-wrapper.sh"
if [ -f "$WRAPPER" ]; then
    [ -x "$WRAPPER" ] && pass "wrapper is executable" || fail "wrapper NOT executable"
    grep -q 'JAVA_HOME' "$WRAPPER" && pass "wrapper sets JAVA_HOME" || warn "wrapper may not set JAVA_HOME"
    grep -q 'LWJGL' "$WRAPPER" && pass "wrapper configures LWJGL" || warn "wrapper may not configure LWJGL"
else
    fail "wrapper script missing: $WRAPPER"
fi

header "8. JAR File"
JAR="$SNAP_ROOT/bin/remixed-dungeon.jar"
if [ -f "$JAR" ]; then
    SIZE=$(du -h "$JAR" | cut -f1)
    pass "JAR exists ($SIZE)"
    if java -jar "$JAR" --version >/dev/null 2>&1; then
        pass "JAR is valid"
    else
        warn "JAR --version check failed (may be normal for game)"
    fi
else
    fail "JAR missing: $JAR"
fi

header "9. Assets"
ASSETS="$SNAP_ROOT/data/mods/Remixed"
if [ -d "$ASSETS" ]; then
    pass "Assets dir exists"
    COUNT=$(find "$ASSETS" -type f | wc -l)
    [ "$COUNT" -gt 0 ] && pass "Assets: $COUNT files" || fail "Assets dir is empty"
else
    fail "Assets dir missing: $ASSETS"
fi

header "10. ALSA Config"
if [ -f "$SNAP_ROOT/etc/asound.conf" ]; then
    pass "asound.conf exists"
else
    fail "asound.conf missing"
fi
if [ -d "$SNAP_ROOT/usr/lib/x86_64-linux-gnu/alsa-lib" ]; then
    pass "alsa-lib dir exists"
else
    fail "alsa-lib dir missing"
fi

header "11. Java Full Settings (if java works)"
if [ -x "$JAVA_BIN" ]; then
    echo "  Running java -XshowSettings:all -version 2>&1 | head -60"
    "$JAVA_BIN" -XshowSettings:all -version 2>&1 | head -60
    echo "  ..."
fi

header "12. Quick Launch Test (background, 5 sec)"
if [ -x "$JAVA_BIN" ] && [ -f "$JAR" ]; then
    LOGFILE="${SNAP_USER_DATA:-/tmp}/investigate-launch.log"
    echo "  Launching game in background, will wait 5 seconds..."
    "$JAVA_BIN" \
        --add-opens java.base/java.util=ALL-UNNAMED \
        -Dassets.dir="$SNAP_ROOT/data" \
        -Duser.home="${SNAP_USER_DATA:-/tmp}" \
        -Djava.io.tmpdir="${SNAP_USER_DATA:-/tmp}/tmp" \
        -Djava.library.path="${SNAP_USER_DATA:-/tmp}/.lwjgl-natives:$SNAP_ROOT/usr/lib/x86_64-linux-gnu:$SNAP_ROOT/usr/lib/jni:$SNAP_ROOT/usr/lib/jvm/java-17-openjdk-amd64/lib" \
        -Dorg.lwjgl.librarypath="${SNAP_USER_DATA:-/tmp}/.lwjgl-natives" \
        -Dorg.lwjgl.util.Debug=true \
        -Dorg.lwjgl.util.DebugLoader=true \
        -jar "$JAR" \
        --windowed > "$LOGFILE" 2>&1 &
    GAME_PID=$!
    # Wait up to 5 seconds
    for i in 1 2 3 4 5; do
        if ! kill -0 "$GAME_PID" 2>/dev/null; then
            break
        fi
        sleep 1
    done
    if kill -0 "$GAME_PID" 2>/dev/null; then
        pass "Game still running after 5 seconds (PID $GAME_PID)"
        kill "$GAME_PID" 2>/dev/null
        wait "$GAME_PID" 2>/dev/null
    else
        wait "$GAME_PID" 2>/dev/null
        LAUNCH_RC=$?
        fail "Game exited with code $LAUNCH_RC within 5 seconds"
    fi
    echo "  Launch log (last 40 lines):"
    tail -40 "$LOGFILE"
fi

header "13. Launch via Wrapper Script (5 sec)"
if [ -f "$WRAPPER" ]; then
    echo "  Running wrapper script in background for 5 seconds..."
    LOGFILE2="${SNAP_USER_DATA:-/tmp}/investigate-wrapper.log"
    bash "$WRAPPER" > "$LOGFILE2" 2>&1 &
    WRAP_PID=$!
    for i in 1 2 3 4 5; do
        if ! kill -0 "$WRAP_PID" 2>/dev/null; then
            break
        fi
        sleep 1
    done
    if kill -0 "$WRAP_PID" 2>/dev/null; then
        pass "Wrapper: game still running after 5 seconds (PID $WRAP_PID)"
        kill "$WRAP_PID" 2>/dev/null
        wait "$WRAP_PID" 2>/dev/null
    else
        wait "$WRAP_PID" 2>/dev/null
        WRAP_RC=$?
        fail "Wrapper: game exited with code $WRAP_RC within 5 seconds"
    fi
    echo "  Wrapper log (last 40 lines):"
    tail -40 "$LOGFILE2"
fi

header "14. Launch via alsa-launch + wrapper (simulating snap run)"
ALSALAUNCH="$SNAP_ROOT/snap/command-chain/alsa-launch"
if [ -f "$ALSALAUNCH" ]; then
    pass "alsa-launch exists"
    echo "  Running alsa-launch + wrapper for 5 seconds..."
    LOGFILE3="${SNAP_USER_DATA:-/tmp}/investigate-alsa-wrapper.log"
    bash "$ALSALAUNCH" "$WRAPPER" > "$LOGFILE3" 2>&1 &
    ALSA_PID=$!
    for i in 1 2 3 4 5; do
        if ! kill -0 "$ALSA_PID" 2>/dev/null; then
            break
        fi
        sleep 1
    done
    if kill -0 "$ALSA_PID" 2>/dev/null; then
        pass "alsa-launch+wrapper: game still running after 5 seconds (PID $ALSA_PID)"
        kill "$ALSA_PID" 2>/dev/null
        wait "$ALSA_PID" 2>/dev/null
    else
        wait "$ALSA_PID" 2>/dev/null
        ALSA_RC=$?
        fail "alsa-launch+wrapper: game exited with code $ALSA_RC within 5 seconds"
    fi
    echo "  alsa-launch+wrapper log (last 40 lines):"
    tail -40 "$LOGFILE3"
else
    fail "alsa-launch not found: $ALSALAUNCH"
fi

header "15. LWJGL Natives Check"
NATIVES_DIR="${SNAP_USER_DATA:-/tmp}/.lwjgl-natives"
if [ -d "$NATIVES_DIR" ]; then
    NATIVE_COUNT=$(find "$NATIVES_DIR" -name '*.so' 2>/dev/null | wc -l)
    pass "LWJGL natives dir exists: $NATIVE_COUNT .so files"
    find "$NATIVES_DIR" -name '*.so' -exec basename {} \; 2>/dev/null | sort | while read f; do
        echo "    $f"
    done
else
    warn "No LWJGL natives extracted yet (will extract on first run)"
fi

header "16. Environment Diff (key vars)"
for var in DISPLAY WAYLAND_DISPLAY LD_LIBRARY_PATH LIBGL_DRIVERS_PATH JAVA_HOME XDG_RUNTIME_DIR DBUS_SESSION_BUS_ADDRESS; do
    VAL="${!var}"
    if [ -n "$VAL" ]; then
        echo "  $var=$VAL"
    else
        warn "$var is NOT SET"
    fi
done

header "17. Game CWD Log Files"
echo "  CWD=$(pwd)"
for f in stdout.log stderr.log; do
    if [ -f "$f" ]; then
        SIZE=$(wc -c < "$f")
        if [ "$SIZE" -gt 0 ]; then
            fail "$f exists with content ($SIZE bytes):"
            tail -30 "$f" | sed 's/^/    /'
        else
            warn "$f exists but is EMPTY ($SIZE bytes) — game redirected output before crashing"
        fi
    else
        echo "  $f not found in CWD"
    fi
done
# Also check $SNAP_USER_DATA/.local/share/remixed-dungeon/
GAME_DATA="${SNAP_USER_DATA:-$HOME/snap/remixed-dungeon/current}/.local/share/remixed-dungeon"
if [ -d "$GAME_DATA" ]; then
    pass "Game data dir: $GAME_DATA"
    ls -la "$GAME_DATA" 2>/dev/null | sed 's/^/    /'
    for f in stdout.log stderr.log; do
        if [ -f "$GAME_DATA/$f" ] && [ -s "$GAME_DATA/$f" ]; then
            echo "  $GAME_DATA/$f:"
            tail -30 "$GAME_DATA/$f" | sed 's/^/    /'
        fi
    done
else
    echo "  No game data dir at $GAME_DATA"
fi

header "18. Launch with --debug (keeps stdout/stderr on terminal)"
if [ -x "$JAVA_BIN" ] && [ -f "$JAR" ]; then
    LOGFILE_DBG="${SNAP_USER_DATA:-/tmp}/investigate-debug.log"
    echo "  Launching with --debug flag for 8 seconds..."
    "$JAVA_BIN" \
        --add-opens java.base/java.util=ALL-UNNAMED \
        -Dassets.dir="$SNAP_ROOT/data" \
        -Duser.home="${SNAP_USER_DATA:-/tmp}" \
        -Djava.io.tmpdir="${SNAP_USER_DATA:-/tmp}/tmp" \
        -Djava.library.path="${SNAP_USER_DATA:-/tmp}/.lwjgl-natives:$SNAP_ROOT/usr/lib/x86_64-linux-gnu:$SNAP_ROOT/usr/lib/jni:$SNAP_ROOT/usr/lib/jvm/java-17-openjdk-amd64/lib" \
        -Dorg.lwjgl.librarypath="${SNAP_USER_DATA:-/tmp}/.lwjgl-natives" \
        -jar "$JAR" \
        --windowed \
        --debug > "$LOGFILE_DBG" 2>&1 &
    GAME_PID=$!
    for i in 1 2 3 4 5 6 7 8; do
        if ! kill -0 "$GAME_PID" 2>/dev/null; then
            break
        fi
        sleep 1
    done
    if kill -0 "$GAME_PID" 2>/dev/null; then
        pass "Debug launch: game still running after 8 seconds (PID $GAME_PID)"
        kill "$GAME_PID" 2>/dev/null
        wait "$GAME_PID" 2>/dev/null
    else
        wait "$GAME_PID" 2>/dev/null
        DBG_RC=$?
        fail "Debug launch: game exited with code $DBG_RC within 8 seconds"
    fi
    echo "  Debug log (last 60 lines):"
    tail -60 "$LOGFILE_DBG"
fi

header "SUMMARY"
echo -e "  ${GREEN}PASS${NC}: $PASS  ${RED}FAIL${NC}: $FAIL  ${YELLOW}WARN${NC}: $WARN"
[ "$FAIL" -eq 0 ] && echo -e "  ${GREEN}All checks passed${NC}" || echo -e "  ${RED}$FAIL check(s) failed${NC}"
exit $FAIL
