package com.nyrds.platform.app;

import java.io.IOException;

/**
 * WebServer stub for the HTML platform.
 * TeaVM has no server sockets - the http server is Android/desktop only.
 * Exists only so main sources compile; AboutScene guards every call
 * behind Utils.isAndroid(), so this is never instantiated or started.
 * start() mirrors BaseWebServer's throws clause so its callers compile.
 */
public class WebServer {
    public WebServer(int port) {
    }

    public static boolean isRunning() {
        return false;
    }

    public static String getServerAddress() {
        return "";
    }

    public void start() throws IOException {
    }
}
