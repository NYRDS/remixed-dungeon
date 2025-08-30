package com.nyrds.pixeldungeon.html;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.nyrds.platform.game.RemixedDungeon;

public class HtmlLauncher extends GwtApplication {
    @Override
    public GwtApplicationConfiguration getConfig() {
        GwtApplicationConfiguration config = new GwtApplicationConfiguration(800, 480);
        // In GWT, we set the title differently
        // config.title = "Remixed Dungeon"; // This field doesn't exist in GWT
        return config;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new RemixedDungeon();
    }
}