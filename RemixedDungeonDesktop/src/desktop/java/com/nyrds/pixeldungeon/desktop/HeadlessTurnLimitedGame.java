package com.nyrds.pixeldungeon.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.QuickModTest;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A headless game runner that runs the game for a specified number of turns before terminating
 */
public class HeadlessTurnLimitedGame extends QuickModTest {

    private int maxTurns;
    private int currentTurns = 0;
    private boolean gameTerminated = false;
    private float lastActorTime = 0f;
    private Thread turnCheckThread;

    private String modName;

    public HeadlessTurnLimitedGame(String modName, int maxTurns) {
        this.modName = modName;
        this.maxTurns = maxTurns;
    }

    @Override
    public void create() {
        super.create();

        // Set scene mode to run levels test and select the mod after LibGDX is initialized
        Scene.setMode(Scene.LEVELS_TEST);
        ModdingMode.selectMod(modName);

        // Start the turn checking thread when the game is created
        startTurnChecking();
    }

    private void startTurnChecking() {
        turnCheckThread = new Thread(() -> {
            while (!gameTerminated) {
                try {
                    Thread.sleep(100); // Check every 100ms for more responsive checking

                    if (gameTerminated) {
                        break;
                    }

                    // Check if game time has advanced (indicating turns have passed)
                    checkTurns();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        turnCheckThread.setDaemon(true);
        turnCheckThread.setName("TurnChecker");
        turnCheckThread.start();
    }

    /**
     * This method checks for turn advancement
     * Since the actual game turns are managed by the Actor system, we monitor
     * the Actor.localTime to detect when turns have passed
     */
    private void checkTurns() {
        if (gameTerminated) {
            return;
        }

        // Check if game time has advanced (indicating turns have passed)
        float currentActorTime = Actor.localTime();
        if (currentActorTime > lastActorTime) {
            // Calculate turns passed based on time difference
            // Each turn typically advances time by TICK (1f) but may vary
            float timeDiff = currentActorTime - lastActorTime;
            int turnsPassed = (int) Math.floor(timeDiff / Actor.TICK);

            if (turnsPassed > 0) {
                currentTurns += turnsPassed;
                lastActorTime = currentActorTime;

                GLog.debug("Turn counter: %d / %d", currentTurns, maxTurns);

                // Check if we've reached the turn limit
                if (maxTurns > 0 && currentTurns >= maxTurns) {
                    GLog.i("Reached turn limit: " + maxTurns + " (actual: " + currentTurns + ")");
                    terminateGame();
                }
            }
        }
    }

    private void terminateGame() {
        gameTerminated = true;
        GLog.i("Terminating game after " + currentTurns + " turns");

        // Schedule termination on the main UI thread
        GameLoop.pushUiTask(() -> {
            try {
                // Switch to title scene to cleanly exit
                GameLoop.switchScene(TitleScene.class);

                // Give it a moment to switch scenes and clean up
                Thread terminationThread = new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        System.exit(0);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                terminationThread.start();
            } catch (Exception e) {
                GLog.w("Error terminating game: %s", e.getMessage());
                System.exit(1);
            }
        });
    }

    @Override
    public void dispose() {
        gameTerminated = true;
        if (turnCheckThread != null) {
            turnCheckThread.interrupt();
        }
        super.dispose();
    }

    /**
     * Main method to run the headless turn-limited game
     */
    public static void main(String[] args) {
        String modName = "Remixed"; // Default mod
        int maxTurns = 0; // Default: run indefinitely

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
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: java -jar RemixedDungeon.jar --mod MOD_NAME --turns MAX_TURNS");
                System.out.println("  MOD_NAME: Name of the mod to run (default: Remixed)");
                System.out.println("  MAX_TURNS: Maximum number of turns to run (default: 0, run indefinitely)");
                System.exit(0);
            }
        }

        // Clear the log file on start
        clearLogFile();

        // Set up the application configuration for headless mode
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Remixed Pixel Dungeon - Headless Turn Limited");
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        cfg.setForegroundFPS(60);
        cfg.setWindowedMode(800, 480);
        cfg.setResizable(false); // Non-resizable for consistency
        cfg.enableGLDebugOutput(true, System.err);

        // Disable window creation for headless operation
        cfg.setWindowedMode(1, 1); // Minimal window size

        // Create and start the application with the turn-limited game
        HeadlessTurnLimitedGame game = new HeadlessTurnLimitedGame(modName, maxTurns);
        new Lwjgl3Application(game, cfg);
    }

    private static void clearLogFile() {
        try {
            // Define the log file path using user data directory
            String userDataPath = getUserDataPath();
            new File(userDataPath).mkdirs(); // Ensure directory exists
            String logFilePath = userDataPath + "mods/RePdLogFile.log";

            // Create the file if it doesn't exist
            File logFile = new File(logFilePath);
            if (!logFile.exists()) {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }

            // Clear the file content
            Files.write(Paths.get(logFilePath), new byte[0]);

            System.out.println("Log file cleared successfully.");
        } catch (IOException e) {
            System.err.println("Failed to clear log file: " + e.getMessage());
        }
    }

    private static String getUserDataPath() {
        // Check if SNAP_USER_DATA is available via user.home system property
        String snapUserData = System.getProperty("user.home");
        if (snapUserData != null && !snapUserData.isEmpty()) {
            // Use a specific subdirectory for Remixed Dungeon data in user's home
            return snapUserData + System.getProperty("file.separator") + ".local" +
                   System.getProperty("file.separator") + "share" +
                   System.getProperty("file.separator") + "remixed-dungeon" +
                   System.getProperty("file.separator");
        }

        // Fallback to the original relative path behavior
        return "";
    }
}