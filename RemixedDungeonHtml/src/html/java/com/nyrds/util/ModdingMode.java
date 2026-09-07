package com.nyrds.util;

import com.badlogic.gdx.graphics.Pixmap;
import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.storage.Assets;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// Stub class for LuaError
class LuaError extends RuntimeException {
    public LuaError(String message) {
        super(message);
    }
    
    public LuaError(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * HTML version of ModdingMode
 */
@LuaInterface
public class ModdingMode extends ModdingBase {
    public static boolean useRetroHeroSprites = false;
    private static boolean mTextRenderingMode = false;
    
    public static void selectMod(String mod) {
        // HTML version doesn't support mod selection
    }
    
    public static String getActiveMod() {
        return "Remixed";
    }
    
    public static boolean isModdingMode() {
        return false;
    }
    
    public static boolean isAssetExist(String resName) {
        return Assets.exists(resName);
    }
    
    public static String text(String id, Object... args) {
        // Simple implementation for HTML version
        return id;
    }
    
    public static String getRemixedVersion() {
        return BuildConfig.VERSION_NAME;
    }
    
    // Additional methods needed for HTML version
    public static boolean isResourceExist(String fileName) {
        // must match getInputStream: callers build fallback chains on this
        // (e.g. the variative tilemap desc falls back to tiles_x_default.json)
        return Assets.exists(fileName);
    }

    public static boolean isResourceExistInMod(String resName) {
        // no mod directories on web
        return false;
    }
    
    public static InputStream getInputStream(String fileName) {
        if (!Assets.exists(fileName)) {
            jsLog("getInputStream: NOT EXISTS " + fileName);
            return null;
        }
        try {
            return Assets.getFile(fileName).read();
        } catch (Exception e) {
            jsLog("getInputStream: READ FAILED " + fileName + " : " + e);
            EventCollector.logException(e, "getInputStream " + fileName);
            return null;
        }
    }

    public static String getResource(String fileName) {
        if (!Assets.exists(fileName)) {
            return null;
        }
        try {
            return Assets.getText(fileName);
        } catch (Exception e) {
            EventCollector.logException(e, "getResource " + fileName);
            return null;
        }
    }

    public static boolean isResourceExists(String fileName) {
        return isAssetExist(fileName);
    }
    
    public static boolean isSoundExists(String soundName) {
        String resourceId = "sound/" + soundName;
        return !getSoundById(resourceId).isEmpty();
    }

    /**
     * Desktop parity (minus mod branches): resolves a sound id to an actual
     * asset file by trying .ogg/.mp3 suffixes, stripping an extension from
     * the id if it already has one.
     */
    public static String getSoundById(String id) {
        if (id.isEmpty()) {
            return "";
        }

        String candidate = id + ".ogg";

        if (isAssetExist(candidate)) {
            return candidate;
        }

        candidate = id + ".mp3";
        if (isAssetExist(candidate)) {
            return candidate;
        }

        if (id.contains(".mp3")) {
            return getSoundById(id.replace(".mp3", ""));
        }

        if (id.contains(".ogg")) {
            return getSoundById(id.replace(".ogg", ""));
        }

        return "";
    }

    @org.teavm.jso.JSBody(params = "text", script = "console.error('MODDING: ' + text);")
    private static native void jsLog(String text);
    
    // Methods needed to fix compilation errors
    public static RuntimeException modException(Exception e) {
        return new RuntimeException("Mod error: " + e.getMessage(), e);
    }
    
    public static RuntimeException modException(String s, Exception e) {
        return new RuntimeException("Mod error: " + s + " - " + e.getMessage(), e);
    }
    
    public static RuntimeException modException(ClassCastException e) {
        return new RuntimeException("Mod error: Class cast exception - " + e.getMessage(), e);
    }
    
    public static RuntimeException modException(LuaError e) {
        return new RuntimeException("Mod error: Lua error - " + e.getMessage(), e);
    }
    
    public static boolean getClassicTextRenderingMode() {
        return mTextRenderingMode;
    }
    
    public static void setClassicTextRenderingMode(boolean val) {
        mTextRenderingMode = val;
    }
    
    public static BitmapData getBitmapData(String src) {
        if (!Assets.exists(src)) {
            EventCollector.logException(new Exception("missing asset: " + src));
            return new BitmapData(1, 1);
        }
        try {
            return new BitmapData(new Pixmap(
                    Assets.getFile(src)));
        } catch (Exception e) {
            EventCollector.logException(e, "bad bitmap: " + src);
            return new BitmapData(1, 1);
        }
    }
    
    public static List<String> listResources(String path, FilenameFilter filter) {
        // In HTML version, we return an empty list
        return new ArrayList<>();
    }
}