package com.nyrds.pixeldungeon.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.windows.WndAlchemy;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.SystemTime;

public class AlchemyWindowLauncher extends RemixedDungeon {

    // A simple scene to host the alchemy window
    public static class AlchemyScene extends PixelScene {

        @Override
        public void create() {
            super.create();

            // Add the alchemy window to this scene
            try {
                add(new WndAlchemy());
            } catch (Exception e) {
                GLog.w("Error creating alchemy window: " + e.getMessage());
                EventCollector.logException(e);
            }

            fadeIn();
        }

        @Override
        public void update() {
            super.update();

            long time =  GameLoop.getMonotoneMillis();

            if (time >= 30f * 1000) {
                Gdx.app.exit();
            }
        }
    }

    @Override
    public void create() {
        super.create();

        // Initialize static handlers that are required for item creation
        // Following the same pattern as FactorySpriteGenerator
        try {
            // Initialize static handlers that are required for item creation
            // This mimics part of what happens in Dungeon.init()
            // This must happen AFTER the framework is initialized to avoid null Gdx.files
            Wand.initWoods();
            Ring.initGems();
            Scroll.initLabels();
            Potion.initColors();

            // Initialize the hero to avoid null pointer exceptions
            Dungeon.hero = new Hero();
        } catch (Exception e) {
            GLog.w("Error initializing static handlers: " + e.getMessage());
        }

        // Switch to AlchemyScene shortly after start
        RemixedDungeon.switchNoFade(AlchemyScene.class);
    }

    public static void main(String[] args) {
        // Configure the application
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Remixed Dungeon - Alchemy Window");
        config.setWindowedMode(600, 800);
        config.useVsync(true);
        config.setForegroundFPS(60);

        // Create and start the application with the AlchemyWindowLauncher instance
        new Lwjgl3Application(new AlchemyWindowLauncher(), config);
    }
}