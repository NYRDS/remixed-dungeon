package com.nyrds.platform.app;

import com.badlogic.gdx.ApplicationListener;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.util.PUtil;

public class RemixedDungeonApp {
    public static ApplicationListener createApplicationListener() {
        return new RemixedDungeon();
    }

    public static void restartApp() {
        // Restarting is not supported in HTML
        PUtil.slog("app", "Restart not supported in HTML");
    }

    // GC auth is a VK Play desktop-launcher thing; html has no launcher ticket
    public static String gcPersId() {
        return "";
    }

    public static String gcToken() {
        return "";
    }

    public static boolean hasGcTicket() {
        return false;
    }
}