package com.nyrds.pixeldungeon.html;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.nyrds.platform.app.RemixedDungeonApp;

public class HtmlLauncher extends GwtApplication {
    @Override
    public GwtApplicationConfiguration getConfig() {
        GwtApplicationConfiguration config = new GwtApplicationConfiguration(800, 480);
        config.title = "Remixed Dungeon";
        return config;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new RemixedDungeonApp();
    }
}