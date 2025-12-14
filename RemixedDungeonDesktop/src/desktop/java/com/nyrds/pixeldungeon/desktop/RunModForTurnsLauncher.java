package com.nyrds.pixeldungeon.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Launcher that runs the game with a specific mod for a given number of turns
 */
public class RunModForTurnsLauncher {

    public static void main(String[] args) {
        String modName = "Remixed"; // Default mod
        int maxTurns = 0; // Default: run indefinitely
        String levelClass = null; // Default: use the default level sequence

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--mod") && i + 1 < args.length) {
                modName = args[i + 1];
                i++;
            } else if (args[i].equals("--turns") && i + 1 < args.length) {
                try {
                    maxTurns = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (NumberFormatException e) {
                    System.err.println("Invalid turn count: " + args[i + 1]);
                    System.exit(1);
                }
            } else if (args[i].equals("--levelClass") && i + 1 < args.length) {
                levelClass = args[i + 1];
                i++;
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: java -jar RemixedDungeon.jar --mod MOD_NAME --turns MAX_TURNS [--levelClass LEVEL_CLASS]");
                System.out.println("  MOD_NAME: Name of the mod to run (default: Remixed)");
                System.out.println("  MAX_TURNS: Maximum number of turns to run (default: 0, run indefinitely)");
                System.out.println("  LEVEL_CLASS: Fully qualified class name of the level to use (optional)");
                System.exit(0);
            }
        }

        // Set up the application configuration
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Remixed Pixel Dungeon - Turn Limited Runner");
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        cfg.setForegroundFPS(60);
        cfg.setWindowedMode(1, 1); // Minimal window size for headless operation
        cfg.setResizable(false);
        cfg.enableGLDebugOutput(true, System.err);

        // Create and start the application with the turn-limited game
        HeadlessTurnLimitedGame game = new HeadlessTurnLimitedGame(modName, maxTurns);
        game.setLevelClass(levelClass); // Set the level class if provided
        new Lwjgl3Application(game, cfg);
    }
}