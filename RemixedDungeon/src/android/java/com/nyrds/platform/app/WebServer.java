package com.nyrds.platform.app;

import android.util.Base64;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.storage.Assets;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.scenes.AboutScene;
import com.watabou.pixeldungeon.utils.GLog;
import java.io.File;

/**
 * WebServer implementation for Android.
 * Routing, page generation and the save/texture handlers live in
 * BaseWebServer - only platform storage access and platform Base64 remain here.
 */
public class WebServer extends BaseWebServer {
    public WebServer(int port) {
        super(port);
    }

    @Override
    public boolean isReady() {
        // Check if game loop is initialized (Android doesn't use libGDX)
        return GameLoop.instance() != null;
    }

    @Override
    protected void onServerStarted() {
        // Notify AboutScene to refresh the WebServer link
        GameLoop.pushUiTask(AboutScene::refreshWebServerLink);
    }

    @Override
    protected byte[] base64Decode(String data) {
        return Base64.decode(data, Base64.DEFAULT);
    }

    @Override
    protected String base64Encode(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    @Override
    protected boolean isDirectory(String path) {
        GLog.debug("Checking if path is directory: '" + path + "'");

        // Special case: empty path or "/" represents the root directory
        if (path.isEmpty() || path.equals("/")) {
            return true;
        }

        // For Remixed mod, check assets
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            try {
                // Check if this path exists as an asset directory
                String[] assetList = Assets.listAssets(path);
                if (assetList != null && assetList.length > 0) {
                    return true;
                }
            } catch (Exception e) {
                // If we can't list assets, treat as file
                GLog.debug("Failed to list assets for: " + path + " - " + e.getMessage());
            }
            return false;
        }
        // For other mods, check file system
        else {
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
            return modFile.exists() && modFile.isDirectory();
        }
    }

    @Override
    protected String[] listDirectoryContents(String path) {
        // For Remixed mod, get contents from assets
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            try {
                return Assets.listAssets(path);
            } catch (Exception e) {
                GLog.debug("Failed to list assets for directory: " + path + " - " + e.getMessage());
                return null;
            }
        }
        // For other mods, get contents from file system
        else {
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
        // For Remixed mod, check assets
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            try {
                String assetPath = (parentPath.isEmpty() ? "" : parentPath + "/") + itemName;
                String[] assetList = Assets.listAssets(assetPath);
                if (assetList != null && assetList.length > 0) {
                    return true;
                }
            } catch (Exception e) {
                GLog.debug("Failed to check if asset is directory: " + itemName + " - " + e.getMessage());
            }
            return false;
        }
        // For other mods, check file system
        else {
            // Construct the full path properly: ModName/parentPath/itemName
            String fullPath = ModdingMode.activeMod();
            if (!parentPath.isEmpty()) {
                fullPath += "/" + parentPath;
            }
            fullPath += "/" + itemName;

            return FileSystem.getExternalStorageFile(fullPath).isDirectory();
        }
    }
}
