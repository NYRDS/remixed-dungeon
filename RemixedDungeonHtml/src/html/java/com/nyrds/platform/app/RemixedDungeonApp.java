package com.nyrds.platform.app;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.util.PUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemixedDungeonApp extends GwtApplication {
    private static String[] savedArgs = new String[0];

    @Override
    public GwtApplicationConfiguration getConfig() {
        GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(800, 480);
        cfg.title = "Remixed Pixel Dungeon";
        return cfg;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new RemixedDungeon();
    }

    private static void probeModules() {
        // Module probing is not needed in HTML
    }

    public static void restartApp() {
        // Restarting is not supported in HTML
        PUtil.slog("app", "Restart not supported in HTML");
    }
}