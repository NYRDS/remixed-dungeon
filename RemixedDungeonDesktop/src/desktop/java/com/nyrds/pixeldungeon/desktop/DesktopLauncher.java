package com.nyrds.pixeldungeon.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.platform.game.RemixedDungeon;

public class DesktopLauncher {
    public static void main (String[] arg) {
        System.out.println("Its alive!");
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Remixed Dungeon");
        cfg.setWindowedMode(480, 800);
        cfg.setBackBufferConfig(8,8,8,8,16,0,0);
        cfg.enableGLDebugOutput(true, System.err);

        final Lwjgl3Application lwjgl3Application = new Lwjgl3Application(new RemixedDungeon(), cfg);
    }
}