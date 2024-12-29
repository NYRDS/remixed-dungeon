package com.nyrds.platform.storage;

import com.nyrds.platform.game.Game;

import java.io.OutputStream;

public class AndroidSAF {

    public static String mBaseDstPath;
    public static String mBaseSrcPath;

    public static void pickDirectoryForModInstall() {
    }

    public static void setListener(IListener wnd) {
    }

    public static void copyModToAppStorage() {
    }

    public static boolean isAutoSyncMaybeNeeded(String s) {
        return false;
    }

    public static void pickDirectoryForModExport() {
    }

    public static OutputStream outputStreamToDocument(Game instance, String mBaseDstPath, String s) {
        return null;
    }

    public interface IListener {
        void onMessage(String message);
        void onFileCopy(String path);

        void onFileSkip(String path);

        void onComplete();

        void onFileDelete(String entry);
    }
}
