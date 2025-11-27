package com.nyrds.platform.app;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.utils.GLog;

/**
 * Minimal LibGDX Application implementation for Web Server only.
 * This initializes the LibGDX backend with necessary systems to access assets.
 */
public class WebServerOnlyGame implements ApplicationListener {

    private boolean initialized = false;
    private GameLoop gameLoop;

    @Override
    public void create() {
        GLog.i("WebServerOnlyGame: Initializing minimal LibGDX backend for web server");

        // Initialize the GameLoop singleton with a minimal scene to prevent null pointer exceptions
        // We need to pass some scene class to prevent crashes in the logging system
        gameLoop = new GameLoop(TitleScene.class);

        // Initialize modding system with the active mod
        String modName = System.getProperty("remixed.mod", "Remixed");
        ModdingMode.selectMod(modName);

        GLog.i("WebServerOnlyGame: Initialized filesystem and assets for mod: " + modName);
        initialized = true;
    }

    @Override
    public void resize(int width, int height) {
        // Not needed for headless operation
    }

    @Override
    public void render() {
        // Minimal rendering or no rendering for headless operation
        // The web server runs on its own thread, so we don't need to do much here
        if (initialized) {
            // Keep application alive but do minimal work
            // Sleep briefly to avoid consuming too much CPU
            try {
                Thread.sleep(100); // Sleep 100ms between renders
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void pause() {
        // Not needed for headless operation
    }

    @Override
    public void resume() {
        // Not needed for headless operation
    }

    @Override
    public void dispose() {
        GLog.i("WebServerOnlyGame: Disposing resources");
        // Any cleanup needed
    }
}