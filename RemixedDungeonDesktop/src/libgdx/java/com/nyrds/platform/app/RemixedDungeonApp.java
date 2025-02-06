package com.nyrds.platform.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.util.PUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemixedDungeonApp {
    private static String[] savedArgs = new String[0];

    public static void main(String[] args) {
        savedArgs = args; // Save the command-line arguments

        System.setProperty("https.protocols", "TLSv1.2");
        System.setProperty("jdk.module.addOpens", "java.base/java.util=ALL-UNNAMED");

        probeModules();

        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Remixed Dungeon");
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        cfg.setForegroundFPS(30);

        cfg.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        cfg.enableGLDebugOutput(true, System.err);


        final Lwjgl3Application app = new Lwjgl3Application(new RemixedDungeon(), cfg);
    }

    private static void probeModules() {
        // Get the module of the current class
        Module currentModule = RemixedDungeonApp.class.getModule();

        // Get the module for java.base
        Module javaBaseModule = Object.class.getModule();

        // Open the java.util package to the current module
        javaBaseModule.addOpens("java.util", currentModule);
    }

    public static void restartApp() {
        try {
            // Get the Java Runtime
            Runtime runtime = Runtime.getRuntime();

            // Determine the operating system
            String os = System.getProperty("os.name").toLowerCase();

            // Create the command to restart the application
            List<String> command = new ArrayList<>();

            if (os.contains("win")) {
                // On Windows, use the RemixedDungeon.exe executable
                String exePath = System.getProperty("user.dir") + "/RemixedDungeon.exe";
                command.add(exePath);

                // Add saved arguments
                command.addAll(Arrays.asList(savedArgs));
            } else {
                // On other platforms, use the Java command
                String java = System.getProperty("java.home") + "/bin/java";
                command.add(java);
                command.add("--add-opens");
                command.add("java.base/java.util=ALL-UNNAMED");
                command.add("-Dhttps.protocols=TLSv1.2");
                command.add("-cp");
                command.add(System.getProperty("java.class.path"));
                command.add(RemixedDungeonApp.class.getName());

                // Add saved arguments
                command.addAll(Arrays.asList(savedArgs));
            }

            PUtil.slog("app", "Restarting application with command: " + String.join(" ", command));

            // Execute the command
            Process process = runtime.exec(command.toArray(new String[0]));

            // Print the process ID of the new process
            System.out.println("Restarting application with process ID: " + process.pid());

            // Exit the current instance of the application
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}