package com.nyrds.pixeldungeon.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.pixeldungeon.windows.WndAlchemy;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.potions.Potion;

public class AlchemyWindowLauncher {

    public static void main(String[] args) {
        // Configure the application
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Remixed Dungeon - Alchemy Window");
        config.setWindowedMode(600, 800);
        config.useVsync(true);
        config.setForegroundFPS(60);

        // Create and start the application with a custom game instance
        new Lwjgl3Application(new RemixedDungeon() {
            @Override
            public void create() {
                super.create();
                // Switch to TitleScene first to ensure proper initialization
                GameLoop.switchScene(TitleScene.class);

                // Schedule the alchemy window to be shown after the game is initialized
                Thread delayedWindowOpener = new Thread(() -> {
                    try {
                        Thread.sleep(2000); // Wait 2 seconds for game to fully initialize

                        // Initialize static handlers that are required for item creation
                        // This mimics part of what happens in Dungeon.init()
                        try {
                            Wand.initWoods();
                            Ring.initGems();
                            Scroll.initLabels();
                            Potion.initColors();
                        } catch (Exception e) {
                            System.out.println("Error initializing static handlers: " + e.getMessage());
                        }

                        // Show the alchemy window
                        GameScene.show(new WndAlchemy());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                delayedWindowOpener.start();
            }
        }, config);
    }
}