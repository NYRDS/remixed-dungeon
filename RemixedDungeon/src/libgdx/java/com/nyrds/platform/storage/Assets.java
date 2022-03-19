package com.nyrds.platform.storage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.SneakyThrows;

public class Assets {
    static public boolean isAssetExits(String assetName) {
        return FileSystem.getInternalStorageFile(assetName).exists();
    }

    @SneakyThrows
    static public String[] listAssets(String path) {
        return FileSystem.getInternalStorageFile(path).list();
    }

    static public InputStream getStream(String name) throws IOException {
        return new FileInputStream(FileSystem.getInternalStorageFile(name));
    }
}
