package com.nyrds.platform.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Headless desktop application that only runs the web server without the game UI
 */
public class WebServerApp {
    private static String[] savedArgs = new String[0];

    public static void main(String[] args) {
        savedArgs = args;

        // Initialize BuildConfig with command line arguments
        BuildConfig.init(args);

        // Set up error handling
        setupUncaughtExceptionHandler();


        System.setProperty("https.protocols", "TLSv1.2");
        System.setProperty("jdk.module.addOpens", "java.base/java.util=ALL-UNNAMED");

        // Parse command line arguments for web server settings
        int webServerPort = parseWebServerPort(args);
        String activeMod = parseActiveMod(args);

        if (webServerPort <= 0) {
            System.err.println("Usage: java -cp ... com.nyrds.platform.app.WebServerApp --webserver=<port> [--mod=<modname>]");
            System.err.println("Example: java -cp ... com.nyrds.platform.app.WebServerApp --webserver=8080 --mod=Remixed");
            System.exit(1);
        }

        if (activeMod != null) {
            System.setProperty("remixed.mod", activeMod);
        }

        try {
            // Add shutdown hook for clean exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down WebServer...");
                // The WebServer doesn't have a stop method, so we just exit
            }));

            // Start the web server in a separate thread because Lwjgl3Application blocks
            Thread serverThread = new Thread(() -> {
                try {
                    WebServer server = new WebServer(webServerPort);
                    server.start();

                    System.out.println("WebServer started successfully!");
                    System.out.println("WebServer is running at: " + WebServer.getServerAddress());
                    System.out.println("Active mod: " + System.getProperty("remixed.mod", "Remixed"));
                    System.out.println("Press Ctrl+C to stop the server.");
                } catch (Exception e) {
                    System.err.println("Failed to start WebServer in thread: " + e.getMessage());
                    e.printStackTrace();
                }
            }, "WebServer-Thread");

            // Start the server thread
            serverThread.start();

            // Give the server a moment to start up before initializing LibGDX
            Thread.sleep(1000); // Wait 1 second

            // Initialize LibGDX with minimal configuration for filesystem access only
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setWindowedMode(1, 1); // Minimal window size
            config.setWindowPosition(-10000, -10000); // Hide window off-screen
            config.setTitle("WebServerApp - Headless Mode"); // Minimal title
            config.setForegroundFPS(1); // Very low FPS to minimize resource usage
            config.setInitialVisible(false); // Keep window hidden

            // Initialize the LibGDX backend with minimal resources using our headless game
            System.out.println("Initializing LibGDX backend for filesystem access...");
            final Lwjgl3Application app = new Lwjgl3Application(new WebServerOnlyGame(), config);

            // Keep the main thread alive to keep the server running
            // Wait for the process to be terminated by a signal (like Ctrl+C)
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Failed to start WebServer: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void setupUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            String stackTrace = stringWriter.toString();

            System.err.println("Unhandled Exception in WebServerApp:\n" + throwable.getMessage());
            System.err.println("Stack Trace:\n" + stackTrace);

            System.exit(1);
        });
    }

    private static int parseWebServerPort(String[] args) {
        for (String arg : args) {
            if (arg != null) {
                if (arg.startsWith("--webserver=") || arg.startsWith("-webserver=")) {
                    try {
                        String portStr = arg.split("=")[1];
                        return Integer.parseInt(portStr);
                    } catch (Exception e) {
                        System.err.println("Invalid web server port: " + arg);
                        return -1;
                    }
                } else if (arg.equals("--webserver") || arg.equals("-webserver")) {
                    return 8080; // default port
                }
            }
        }
        return -1; // No web server argument provided
    }

    private static String parseActiveMod(String[] args) {
        for (String arg : args) {
            if (arg != null) {
                if (arg.startsWith("--mod=") || arg.startsWith("-mod=")) {
                    try {
                        return arg.split("=")[1];
                    } catch (Exception e) {
                        System.err.println("Invalid mod name: " + arg);
                        return null;
                    }
                } else if (arg.startsWith("--mod") || arg.startsWith("-mod")) {
                    // Handle case where mod name is provided without =, like "--mod Remixed"
                    // This would be handled by looking at the next argument, but for now 
                    // we expect --mod=ModName format
                    return null;
                }
            }
        }
        return null;
    }

    private static String getUserDataPath() {
        // Check if SNAP_USER_DATA is available via user.home system property
        String snapUserData = System.getProperty("user.home");
        if (snapUserData != null && !snapUserData.isEmpty()) {
            // Use a specific subdirectory for Remixed Dungeon data in user's home
            return snapUserData + System.getProperty("file.separator") + ".local" +
                   System.getProperty("file.separator") + "share" +
                   System.getProperty("file.separator") + "remixed-dungeon-webserver" +
                   System.getProperty("file.separator");
        }

        // Fallback to the original relative path behavior
        return "";
    }
}