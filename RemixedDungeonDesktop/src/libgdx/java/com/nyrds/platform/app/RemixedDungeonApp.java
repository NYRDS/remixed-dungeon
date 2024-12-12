package com.nyrds.platform.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.platform.game.RemixedDungeon;

public class RemixedDungeonApp {
    static public boolean checkOwnSignature() {
        return true;
    }
    public static void main(String[] args) {
        System.setProperty("https.protocols", "TLSv1.2");

        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Remixed Dungeon");
        cfg.setWindowedMode(480, 800);
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        cfg.enableGLDebugOutput(true, System.err);

        final Lwjgl3Application app = new Lwjgl3Application(new RemixedDungeon(), cfg);
    }
}
