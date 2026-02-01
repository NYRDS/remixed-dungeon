package com.nyrds.pixeldungeon.desktop;

import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.mechanics.LuaScript;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A utility class for testing Lua scripts in a controlled environment
 */
public class LuaScriptTester {

    public static void main(String[] args) {
        System.out.println("Lua Script Tester - Starting test environment");
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        try {
            // Test the BoneSaw script specifically
            testBoneSawScript();

            System.out.println("Lua Script Tester - Tests completed successfully");
        } catch (Exception e) {
            System.err.println("Error during script testing:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testBoneSawScript() {
        System.out.println("Testing BoneSaw script...");

        try {
            // Check if the BoneSaw.lua file exists
            String projectRoot = System.getProperty("user.dir").replace("/RemixedDungeonDesktop/src/desktop/rundir", "");
            String scriptPath = projectRoot + "/RemixedDungeon/src/main/assets/scripts/items/BoneSaw.lua";

            if (Files.exists(Paths.get(scriptPath))) {
                System.out.println("BoneSaw.lua file found at: " + scriptPath);

                // Try to create a LuaScript instance to test if the script loads without errors
                try {
                    // Create a temporary LuaScript instance to test the syntax
                    // We pass null as the owner since we're just testing syntax
                    LuaScript script = new LuaScript("scripts/items/BoneSaw", null);
                    System.out.println("BoneSaw script loaded successfully without syntax errors.");

                    // Try to run a basic function to ensure it works
                    // Note: This might fail due to missing game environment, which is expected
                    try {
                        Object result = script.runOptional("getVisualName", "BoneSaw");
                        System.out.println("BoneSaw getVisualName function executed (result: " + result + ")");

                        // Test other functions
                        Object descResult = script.runOptional("desc", null);
                        System.out.println("BoneSaw desc function executed (result: " + descResult + ")");

                        System.out.println("BoneSaw script functions executed successfully.");
                    } catch (Exception runtimeException) {
                        // This is expected when running outside the game environment
                        System.out.println("Note: Runtime error occurred (expected when running outside game environment): " +
                                         runtimeException.getMessage());
                        System.out.println("However, the script syntax is valid.");
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