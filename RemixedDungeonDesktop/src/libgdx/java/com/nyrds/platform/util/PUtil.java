package com.nyrds.platform.util;

import com.badlogic.gdx.utils.Logger;

import java.util.UUID;

public class PUtil {

    static Logger logger = new Logger("slog");

    static public boolean isConnectedToInternet() {

        return false;
    }

    static public void slog(String tag, String txt) {
        // caveman: Gdx.app null before libgdx init. early log calls (webserver
        // startup, pre-init errors) must not crash - fall back to stdout.
        if (com.badlogic.gdx.Gdx.app == null) {
            System.out.println("[" + tag + "] " + txt);
            return;
        }
        logger.setLevel(Logger.INFO);
        logger.info(txt);
    }

    static public UUID getUserId(){
        return UUID.randomUUID();
    }


    public static long getAvailableInternalMemorySize() {
        return 1024 * 1024 * 1024;
    }
}
