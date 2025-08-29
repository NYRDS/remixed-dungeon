package com.nyrds.util;

import com.nyrds.pixeldungeon.ml.BuildConfig;

import java.io.InputStream;

/**
 * HTML version of ModdingMode
 */
public class ModdingMode extends ModdingBase {
    public static boolean useRetroHeroSprites = false;
    
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
        // In HTML version, we assume all assets exist
        return true;
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
        // In HTML version, we can't get input streams directly
        return null;
    }
    
    public static String getResource(String fileName) {
        // In HTML version, we return the file name as resource
        return fileName;
    }
    
    public static boolean isResourceExists(String fileName) {
        // In HTML version, we assume all resources exist
        return true;
    }
    
    public static boolean isSoundExists(String soundName) {
        // In HTML version, we assume all sounds exist
        return true;
    }
}