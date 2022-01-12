package com.nyrds.platform.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.platform.game.RemixedDungeon;

public class RemixedDungeonApp  {
    static public boolean checkOwnSignature() {
        return true;
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Remixed Dungeon");
        cfg.setWindowedMode(480, 800);

        final Lwjgl3Application lwjgl3Application = new Lwjgl3Application(new RemixedDungeon(), cfg);
    }
}
