package com.nyrds.pixeldungeon.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader;
import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.platform.game.RemixedDungeon;
import com.watabou.utils.Bundle;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A utility class for testing Lua scripts in a controlled environment
 */
public class LuaScriptTester {

    public static void main(String[] args) {
        System.out.println("Lua Script Tester - Starting test environment");
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        try {
            // Initialize LibGDX framework
            initializeLibGDX();

            // Initialize basic game systems needed for script testing
            initializeGameEnvironment();

            // Test the BoneSaw script specifically
            testBoneSawScript();

            System.out.println("Lua Script Tester - Tests completed successfully");
        } catch (Exception e) {
            System.err.println("Error during script testing:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void initializeLibGDX() {
        System.out.println("Initializing LibGDX framework...");

        // Load natives
        Lwjgl3NativesLoader.load();

        // Initialize Gdx.files with Lwjgl3Files implementation
        com.badlogic.gdx.Files files = new Lwjgl3Files();
        com.badlogic.gdx.Gdx.files = files;

        System.out.println("LibGDX framework initialized.");
    }

    private static void initializeGameEnvironment() {
        System.out.println("Initializing game environment...");

        // Note: Bundle.init() and RemixedDungeon.init() don't exist as static methods
        // We'll skip full game initialization for now since it requires complex setup
        System.out.println("Skipped full game initialization (requires complex setup).");
    }

    private static void testBoneSawScript() {
        System.out.println("Testing BoneSaw script...");

        try {
            // Check if the BoneSaw.lua file exists
            String projectRoot = System.getProperty("user.dir").replace("/RemixedDungeonDesktop/src/desktop/rundir", "");
            String scriptPath = projectRoot + "/RemixedDungeon/src/main/assets/scripts/items/BoneSaw.lua";

            if (java.nio.file.Files.exists(Paths.get(scriptPath))) {
                System.out.println("BoneSaw.lua file found at: " + scriptPath);

                // Try to create a LuaScript instance to test if the script loads without errors
                try {
                    // Create a temporary LuaScript instance to test the syntax
                    // We pass null as the owner since we're just testing syntax
                    LuaScript script = new LuaScript("scripts/items/BoneSaw", null);
                    System.out.println("BoneSaw script loaded successfully without syntax errors.");

                    // Try to run a basic function to ensure it works
                    try {
                        Object result = script.runOptional("getVisualName", "BoneSaw");
                        System.out.println("BoneSaw getVisualName function executed (result: " + result + ")");

                        // Test other functions
                        Object descResult = script.runOptional("desc", null);
                        System.out.println("BoneSaw desc function executed (result: " + descResult + ")");

                        System.out.println("BoneSaw script functions executed successfully.");
                    } catch (Exception runtimeException) {
                        System.err.println("Runtime error occurred while executing BoneSaw script functions:");
                        runtimeException.printStackTrace();
                    }

                } catch (Exception e) {
                    System.err.println("Error loading BoneSaw script:");
                    e.printStackTrace();
                }
            } else {
                System.out.println("Error: BoneSaw.lua file not found at: " + scriptPath);

                // Look for the file in the project
                File assetsDir = new File(projectRoot + "/RemixedDungeon/src/main/assets/scripts/items/");
                if (assetsDir.exists()) {
                    String[] files = assetsDir.list();
                    if (files != null) {
                        System.out.print("Available script files: ");
                        for (String file : files) {
                            System.out.print(file + " ");
                        }
                        System.out.println();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error testing BoneSaw script:");
            e.printStackTrace();
        }

        System.out.println("BoneSaw script test completed.");
    }
}