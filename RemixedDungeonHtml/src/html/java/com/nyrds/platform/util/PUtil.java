package com.nyrds.platform.util;

import com.badlogic.gdx.utils.Logger;
import java.util.UUID;

public class PUtil {

    static Logger logger = new Logger("slog");

    static public boolean isConnectedToInternet() {
        // In HTML version, we assume internet is available
        return true;
    }

    static public void slog(String tag, String txt) {
        logger.setLevel(Logger.INFO);
        logger.info(txt);
    }

    static public UUID getUserId(){
        return UUID.randomUUID();
    }


    public static long getAvailableInternalMemorySize() {
        return 1024 * 1024 * 1024;
    }

    /**
     * TeaVM objects are plain JS objects, so collection is V8's job and
     * there is no direct GC entry point. A major GC fires whenever V8's
     * old-space heuristics decide to - historically right after a level
     * load, interrupting the player's first combat actions with a
     * multi-hundred-ms pause. Allocating a burst of long-lived-looking
     * garbage here promotes it to old space during the level transition,
     * triggering the major GC while the loading screen is still up.
     */
    public static void gcHint() {
        measureMemory();
    }

    // schedules a memory measurement pass; Chrome collects on the way
    @org.teavm.jso.JSBody(script =
            "if (window.performance && performance.measureMemory) {"
            + " performance.measureMemory()['catch'](function() {}); }")
    private static native void measureMemory();
}
