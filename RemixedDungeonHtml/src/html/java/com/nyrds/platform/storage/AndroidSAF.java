package com.nyrds.platform.storage;

import com.nyrds.platform.game.Game;

import java.io.OutputStream;

/**
 * HTML version of AndroidSAF
 */
public class AndroidSAF {
    // Variables needed for HTML version
    public static String mBaseSrcPath = null;
    public static String mBaseDstPath = null;
    
    public interface IListener {
        void onMessage(String message);
        void onFileCopy(String path);
        void onFileSkip(String path);
        void onComplete();
        void onFileDelete(String entry);
    }
    
    public static void selectFile(IListener listener, String mimeType) {
        // In HTML version, file selection is not supported
        System.out.println("File selection not supported in HTML version");
        listener.onComplete(); // Use onComplete instead of removed onFileSelectionCancelled
    }
    
    public static void saveFile(IListener listener, String mimeType, String fileName) {
        // In HTML version, file saving is not supported
        System.out.println("File saving not supported in HTML version");
        listener.onComplete(); // Use onComplete instead of removed onFileSelectionCancelled
    }
    
    public static void setListener(IListener listener) {
        // In HTML version, setting listener is not supported
        System.out.println("Setting listener not supported in HTML version");
    }
    
    public static boolean isAutoSyncMaybeNeeded(String s) {
        // In HTML version, auto sync is not needed
        return false;
    }
    
    // Methods needed for HTML version (dummy implementations)
    public static void pickDirectoryForModInstall() {
        // In HTML version, directory picking is not supported
        System.out.println("Directory picking for mod install not supported in HTML version");
    }
    
    public static void pickDirectoryForModExport() {
        // In HTML version, directory picking is not supported
        System.out.println("Directory picking for mod export not supported in HTML version");
    }
    
    public static void copyModToAppStorage() {
        // In HTML version, copying mods is not supported
        System.out.println("Copying mods to app storage not supported in HTML version");
    }
    
    public static OutputStream outputStreamToDocument(Game game, String path, String filename) {
        // In HTML version, creating output streams is not supported
        System.out.println("Creating output stream to document not supported in HTML version");
        return null;
    }
}