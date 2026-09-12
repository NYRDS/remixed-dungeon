package com.nyrds.platform.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.GLog;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * WebServer implementation for desktop platforms.
 * Routing, page generation and the save/texture handlers live in
 * BaseWebServer - only platform storage access, platform Base64 and the
 * GL screenshot hook remain here.
 */
public class WebServer extends BaseWebServer {
    public WebServer(int port) {
        super(port);
    }

    @Override
    public boolean isReady() {
        // Check if libGDX files is available (indicates libGDX is initialized)
        try {
            return Gdx.files != null && GameLoop.instance() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onServerStarted() {
        // Desktop server started without special handling needed
    }

    @Override
    protected byte[] base64Decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    @Override
    protected String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    // glReadPixels rows run bottom-up; PNG wants top-down
    private static Pixmap flipVertically(Pixmap pixmap) {
        int w = pixmap.getWidth();
        int h = pixmap.getHeight();
        Pixmap flipped = new Pixmap(w, h, pixmap.getFormat());
        for (int y = 0; y < h; y++) {
            flipped.drawPixmap(pixmap, 0, y, w, 1, 0, h - 1 - y, w, 1);
        }
        pixmap.dispose();
        return flipped;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if ("/debug/screenshot".equals(uri)) {
            return handleDebugScreenshot(session);
        }
        return super.serve(session);
    }

    private Response handleDebugScreenshot(IHTTPSession session) {
        try {
            int width = Gdx.graphics.getWidth();
            int height = Gdx.graphics.getHeight();

            CountDownLatch latch = new CountDownLatch(1);
            final byte[][] pngData = new byte[1][];
            final String[] error = new String[1];

            GameLoop.pushUiTask(() -> {
                try {
                    Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, width, height);
                    if (pixmap == null) {
                        error[0] = "Failed to capture screenshot";
                        latch.countDown();
                        return;
                    }
                    pixmap = flipVertically(pixmap);

                    // glReadPixels gives bottom-up rows - flip them, otherwise the png is upside down
                    Pixmap flipped = new Pixmap(width, height, Pixmap.Format.RGBA8888);
                    java.nio.ByteBuffer src = pixmap.getPixels();
                    java.nio.ByteBuffer dst = flipped.getPixels();
                    int rowBytes = width * 4;
                    byte[] line = new byte[rowBytes];
                    for (int y = 0; y < height; y++) {
                        src.position((height - 1 - y) * rowBytes).get(line);
                        dst.position(y * rowBytes).put(line);
                    }
                    dst.position(0);
                    pixmap.dispose();

                    FileHandle fileHandle = Gdx.files.local("screenshot_tmp.png");
                    PixmapIO.writePNG(fileHandle, flipped);
                    pngData[0] = fileHandle.readBytes();
                    fileHandle.delete();
                    flipped.dispose();
                } catch (Exception e) {
                    error[0] = "Error capturing screenshot: " + e.getMessage();
                } finally {
                    latch.countDown();
                }
            });

            if (!latch.await(5, TimeUnit.SECONDS)) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Timeout waiting for screenshot\"}");
            }

            if (error[0] != null) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"" + error[0] + "\"}");
            }

            if (pngData[0] == null) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Screenshot data is null\"}");
            }

            return newFixedLengthResponse(Response.Status.OK, "image/png",
                new ByteArrayInputStream(pngData[0]), pngData[0].length);
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected String[] listDirectoryContents(String path) {
        GLog.debug("Listing contents of directory: '" + path + "'");

        try {
            // Use ModdingMode to get a combined list of both assets and external files
            List<String> resourceList = ModdingMode.listResources(path, (dir, name) -> true);

            // Filter the resources to only include direct children, not nested items
            List<String> filteredList = new ArrayList<>();
            for (String resource : resourceList) {
                if (!resource.contains("/")) {
                    filteredList.add(resource);
                }
            }

            GLog.debug("Found " + filteredList.size() + " items in directory: '" + path + "'");

            return filteredList.toArray(new String[0]);
        } catch (Exception e) {
            GLog.debug("Error listing directory contents: " + e.getMessage());
            // Fallback to file system method
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
            if (modFile.exists()) {
                return modFile.list();
            }
            GLog.debug("Filesystem directory does not exist: " + modFile.getAbsolutePath());
            return null;
        }
    }

    @Override
    protected boolean isDirectoryItem(String parentPath, String itemName) {
        // For Remixed mod, check both assets and external files
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            // First check if the item exists as a directory in the mod-specific external storage
            if (isExternalDir(ModdingMode.activeMod(), parentPath, itemName)) {
                return true;
            }

            // Then check if it's a directory in assets
            String assetPath = (parentPath.isEmpty() ? "" : parentPath + "/") + itemName;
            try {
                String[] assetList = FileSystem.listResources(assetPath);
                if (assetList != null && assetList.length > 0) {
                    return true;
                }
            } catch (Exception e) {
                GLog.debug("Item '" + itemName + "' not found as directory in assets: " + e.getMessage());
            }
            return false;
        }
        // For other mods, check both external storage and potentially other sources
        else {
            if (isExternalDir(ModdingMode.activeMod(), parentPath, itemName)) {
                return true;
            }

            String checkPath = (parentPath.isEmpty() ? "" : parentPath + "/") + itemName;
            try {
                String[] assetList = FileSystem.listResources(checkPath);
                if (assetList != null && assetList.length > 0) {
                    return true;
                }
            } catch (Exception e) {
                GLog.debug("Item '" + itemName + "' not found as directory via ModdingMode: " + e.getMessage());
            }
            return false;
        }
    }

    private static boolean isExternalDir(String mod, String parentPath, String itemName) {
        String fullPath = mod;
        if (!parentPath.isEmpty()) {
            fullPath += "/" + parentPath;
        }
        fullPath += "/" + itemName;

        File itemFile = FileSystem.getExternalStorageFile(fullPath);
        return itemFile.exists() && itemFile.isDirectory();
    }

    @Override
    protected boolean isDirectory(String path) {
        // Special case: empty path or "/" represents the root directory
        if (path.isEmpty() || path.equals("/")) {
            return true;
        }

        // Check both external storage and assets regardless of mod
        File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
        if (modFile.exists() && modFile.isDirectory()) {
            return true;
        }

        try {
            String[] assetList = FileSystem.listResources(path);
            if (assetList != null && assetList.length > 0) {
                return true;
            }
        } catch (Exception e) {
            GLog.debug("Path '" + path + "' not found as directory via ModdingMode: " + e.getMessage());
        }

        return false;
    }

    /**
     * Debug endpoint to test ModdingMode.listResources directly
     */
    @Override
    protected Response serveDebugList(String path) {
        try {
            List<String> resourceList = ModdingMode.listResources(path, (dir, name) -> true);

            StringBuilder response = new StringBuilder();
            response.append("<h1>Debug List Resources for Path: '").append(path).append("'</h1>");
            response.append("<p>Active mod: ").append(ModdingMode.activeMod()).append("</p>");

            response.append("<p>Total resources found: ").append(resourceList.size()).append("</p>");
            response.append("<ul>");
            for (String resource : resourceList) {
                response.append("<li>").append(resource).append("</li>");
            }
            response.append("</ul>");

            // Also show what the direct-children filtering would return
            response.append("<h2>Filtering Logic Results:</h2>");
            List<String> filteredList = new ArrayList<>();
            for (String resource : resourceList) {
                if (!resource.contains("/")) {
                    filteredList.add(resource);
                }
            }

            response.append("<p>After filtering for direct children: ").append(filteredList.size()).append("</p>");
            response.append("<ul>");
            for (String item : filteredList) {
                response.append("<li>").append(item).append("</li>");
            }
            response.append("</ul>");

            return newFixedLengthResponse(Response.Status.OK, "text/html", response.toString());
        } catch (Exception e) {
            GLog.debug("Error in serveDebugList: " + e.getMessage());
            EventCollector.logException(e);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html",
                "<h1>Error in serveDebugList</h1><p>" + e.getMessage() + "</p>");
        }
    }
}
