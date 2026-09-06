package com.nyrds.platform.app.client;

import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.teavm.reflection.ReflectionConfig;

/**
 * The main entry point for the TeaVM build.
 */
public class TeaVMLauncher {
    public static void main(String[] args) {
        // TeaVM's System.out goes nowhere in the browser console (System.err is
        // the only stream that surfaces, via console.error) - merge them so
        // game logs are visible in the web debug loop.
        System.setOut(System.err);

        // Pools.get(Class) falls back to reflective instantiation, which TeaVM
        // cannot serve ("missing no-arg constructor"). Register a direct-alloc
        // pool before anything touches GlyphLayout/PseudoGlyphLayout.
        Pools.set(GlyphRun.class, new Pool<GlyphRun>(64, 8192) {
            @Override
            protected GlyphRun newObject() {
                return new GlyphRun();
            }
        });

        // --- THIS IS THE NEW LINE ---
        // Call this first to enable reflection before the application starts.
        ReflectionConfig.enableReflectionForDebugging();
        
        // Create the configuration for the TeaVM application
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");
        config.width = 800;  // Set your desired canvas width
        config.height = 480; // Set your desired canvas height

        // FreeTypeFontGenerator (SystemText) calls into the emscripten freetype
        // Module - it must be on the page before any font is generated. Loads
        // from <page>/scripts/freetype.js, extracted there by make_webapp.py.
        config.preloadListener = assetLoader -> assetLoader.loadScript("freetype.js");

        // Create and launch the application
        // The first argument is your main game class (the ApplicationListener)
        // The second argument is the configuration
        new TeaApplication(new RemixedDungeon(), config);
    }
}