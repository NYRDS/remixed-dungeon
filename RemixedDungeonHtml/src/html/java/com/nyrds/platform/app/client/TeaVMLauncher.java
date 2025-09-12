package com.nyrds.platform.app.client;

import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.nyrds.platform.game.RemixedDungeon; // Your existing application listener
import com.nyrds.teavm.reflection.ReflectionConfig; // Reflection configuration

/**
 * The main entry point for the TeaVM build.
 */
public class TeaVMLauncher {
    public static void main(String[] args) {
        // --- THIS IS THE NEW LINE ---
        // Call this first to enable reflection before the application starts.
        ReflectionConfig.enableReflectionForDebugging();
        
        // Create the configuration for the TeaVM application
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");
        config.width = 800;  // Set your desired canvas width
        config.height = 480; // Set your desired canvas height

        // Create and launch the application
        // The first argument is your main game class (the ApplicationListener)
        // The second argument is the configuration
        new TeaApplication(new RemixedDungeon(), config);
    }
}