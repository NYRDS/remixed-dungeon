package com.nyrds.platform.storage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * HTML version of Assets
 */
public class Assets {
    
    public static FileHandle getFile(String asset) {
        return Gdx.files.internal(asset);
    }
    
    public static boolean exists(String asset) {
        return Gdx.files.internal(asset).exists();
    }
    
    public static String getText(String asset) {
        return Gdx.files.internal(asset).readString();
    }
}