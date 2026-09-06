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

@LuaInterface


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
        // In HTML version, we assume all resources exist
        return true;
    }
    
    public static boolean isResourceExistInMod(String resName) {
        // In HTML version, we assume all resources exist
        return true;
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
        // In HTML version, we assume all sounds exist
        return true;
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