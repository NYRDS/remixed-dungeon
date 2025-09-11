package com.nyrds.platform.app;

import com.badlogic.gdx.ApplicationListener;
import com.nyrds.pixeldungeon.ml.BuildConfig;
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
}