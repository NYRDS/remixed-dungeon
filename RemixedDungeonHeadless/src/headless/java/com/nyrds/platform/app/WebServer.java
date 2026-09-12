package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.GLog;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

// headless: BaseWebServer platform hooks, no GL screenshot support
public class WebServer extends BaseWebServer {
    public WebServer(int port) {
        super(port);
    }

    @Override
    public boolean isReady() {
        try {
            return GameLoop.instance() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onServerStarted() {
    }

    @Override
    protected byte[] base64Decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    @Override
    protected String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    @Override
    protected String[] listDirectoryContents(String path) {
        GLog.debug("Listing contents of directory: '%s'", path);

        try {
            List<String> resourceList = ModdingMode.listResources(path, (dir, name) -> true);

            List<String> filteredList = new ArrayList<>();
            for (String resource : resourceList) {
                if (!resource.contains("/")) {
                    filteredList.add(resource);
                }
            }

            return filteredList.toArray(new String[0]);
        } catch (Exception e) {
            GLog.debug("Error listing directory contents: %s", e.getMessage());
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
            if (modFile.exists()) {
                return modFile.list();
            }
            return null;
        }
    }

    @Override
    protected boolean isDirectoryItem(String parentPath, String itemName) {
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
            GLog.debug("Item '%s' not found as directory: %s", itemName, e.getMessage());
        }
        return false;
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
        if (path.isEmpty() || path.equals("/")) {
            return true;
        }

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
            GLog.debug("Path '%s' not found as directory: %s", path, e.getMessage());
        }

        return false;
    }

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

            return newFixedLengthResponse(Response.Status.OK, "text/html", response.toString());
        } catch (Exception e) {
            GLog.debug("Error in serveDebugList: %s", e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html",
                    "<h1>Error in serveDebugList</h1><p>" + e.getMessage() + "</p>");
        }
    }
}
