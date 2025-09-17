package com.nyrds.pixeldungeon.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.game.QuickModTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DesktopQuickModTest {
    public static void main (String[] arg) {
        // Clear the log file on start
        clearLogFile();
        
        // Set up the application configuration
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Remixed Pixel Dungeon - Quick Mod Test");
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        cfg.setForegroundFPS(60);
        cfg.setWindowedMode(800, 480);
        cfg.enableGLDebugOutput(true, System.err);

        // Create and start the application with QuickModTest
        new Lwjgl3Application(new QuickModTest(), cfg);
    }
    
    private static void clearLogFile() {
        try {
            // Define the log file path
            String logFilePath = "mods/RePdLogFile.log";
            
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
}