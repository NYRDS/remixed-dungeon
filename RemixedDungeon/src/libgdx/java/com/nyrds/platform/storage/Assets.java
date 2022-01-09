package com.nyrds.platform.storage;

import com.nyrds.platform.app.RemixedDungeonApp;

import java.io.IOException;
import java.io.InputStream;

import lombok.SneakyThrows;

public class Assets {
    static public boolean isAssetExits(String assetName) {
        InputStream str;
        try {
            str = RemixedDungeonApp.getContext().getAssets().open(assetName);
            str.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @SneakyThrows
    static public String[] listAssets(String path) {
        return RemixedDungeonApp.getContext().getAssets().list(path);
    }

    static public InputStream getStream(String name) throws IOException {
        return RemixedDungeonApp.getContext().getAssets().open(name);
    }
}
