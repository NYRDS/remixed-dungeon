package com.nyrds.platform.util;

import com.badlogic.gdx.utils.Logger;

public class PUtil {

    static Logger logger = new Logger("slog");

    static public boolean isConnectedToInternet() {

        return false;
    }

    static public void slog(String tag, String txt) {
        logger.setLevel(Logger.INFO);
        logger.info(txt);
    }

}
