package com.nyrds.platform.storage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.nyrds.platform.util.PUtil;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaseInsensitiveFileCache {

    private final Map<String, FileHandle> fileCache;


    public CaseInsensitiveFileCache(String... rootPaths) {
        this.fileCache = new HashMap<>();

        for (int i = rootPaths.length - 1; i >= 0; --i) {
            cacheFiles(rootPaths[i]);
        }

        for (String key : fileCache.keySet()) {
            PUtil.slog("file", "Cached file: " + key);
        }
    }

    private void cacheFiles(String... rootPaths) {
        for (String rootPath : rootPaths) {
            FileHandle rootDir = Gdx.files.internal(rootPath);
            if (rootDir.exists() && rootDir.isDirectory()) {
                cacheFilesRecursive(rootDir, rootDir.path());
            } else {
                System.err.println("Root directory does not exist or is not a directory: " + rootPath);
            }
        }
    }

    private void cacheFilesRecursive(FileHandle directory, String prefix) {
        for (FileHandle file : directory.list()) {
            String relativePath = file.path().substring(prefix.length() + 1);
            String lowerCaseRelativePath = relativePath.toLowerCase();

            fileCache.put(lowerCaseRelativePath, file);
            if (file.isDirectory()) {
                cacheFilesRecursive(file, prefix);
            }
        }
    }

    @Nullable
    public FileHandle getFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return fileCache.get(lowerCaseName);
    }

    public boolean exists(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return fileCache.containsKey(lowerCaseName);
    }

    public List<FileHandle> getAllFiles() {
        return new ArrayList<>(fileCache.values());
    }

    public List<String> getAllCachedPaths() {
        return new ArrayList<>(fileCache.keySet());
    }
}