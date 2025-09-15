package com.nyrds.platform.app;

import java.io.IOException;

/**
 * Dummy WebServer implementation for desktop platforms.
 * This provides the same API as the Android WebServer but doesn't actually start a server.
 */
public class WebServer {
    private static WebServer instance = null;
    private String serverAddress = null;
    private boolean started = false;
    
    public WebServer(int port) {
        instance = this;
        serverAddress = "http://localhost:" + port;
    }
    
    public static boolean isRunning() {
        return instance != null && instance.started;
    }
    
    public static String getServerAddress() {
        if (instance != null && instance.serverAddress != null) {
            return instance.serverAddress;
        }
        return "http://localhost:8080";
    }
    
    public void start() throws IOException {
        // Dummy implementation - doesn't actually start a server on desktop
        started = true;
    }
}