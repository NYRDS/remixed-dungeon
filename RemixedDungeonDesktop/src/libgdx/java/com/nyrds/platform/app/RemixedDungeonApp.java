package com.nyrds.platform.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.RemixedDungeon;

import java.io.IOException;

public class RemixedDungeonApp {
    static public boolean checkOwnSignature() {
        return true;
    }
    public static void main(String[] args) {
        System.setProperty("https.protocols", "TLSv1.2");
        System.setProperty("jdk.module.addOpens", "java.base/java.util=ALL-UNNAMED");

        probeModules();

        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Remixed Dungeon");
        cfg.setWindowedMode(480, 800);
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        cfg.enableGLDebugOutput(true, System.err);
        cfg.setForegroundFPS(30);

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

            // Get the path to the current Java executable
            String java = System.getProperty("java.home") + "/bin/java";

            // Get the classpath of the current application
            String classPath = System.getProperty("java.class.path");

            // Get the main class name
            String mainClass = RemixedDungeonApp.class.getName();

            // Create the command to restart the application
            String[] command = new String[]{java,"--add-opens", "java.base/java.util=ALL-UNNAMED", "-Dhttps.protocols=TLSv1.2",  "-cp", classPath, mainClass};

            // Execute the command
            Process process = runtime.exec(command);

            // Print the process ID of the new process
            System.out.println("Restarting application with process ID: " + process.pid());

            // Exit the current instance of the application
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
