package com.nyrds.platform.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

// headless: java.io.File-based, mirrors the desktop cache semantics
public class CaseInsensitiveFileCache {

    private final Map<String, File> fileCache;

    public CaseInsensitiveFileCache(String... rootPaths) {
        this.fileCache = new HashMap<>();

        for (int i = rootPaths.length - 1; i >= 0; --i) {
            cacheFiles(rootPaths[i]);
        }
    }

    private void cacheFiles(String... rootPaths) {
        for (String rootPath : rootPaths) {
            File rootDir = new File(rootPath);
            if (rootDir.exists() && rootDir.isDirectory()) {
                cacheFilesRecursive(rootDir, rootDir.getPath());
            }
        }
    }

    private void cacheFilesRecursive(File directory, String prefix) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            String relativePath = file.getPath().substring(prefix.length() + 1);
            String lowerCaseRelativePath = relativePath.toLowerCase();

            fileCache.put(lowerCaseRelativePath, file);
            if (file.isDirectory()) {
                cacheFilesRecursive(file, prefix);
            }
        }
    }

    @Nullable
    public File getFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return fileCache.get(lowerCaseName);
    }

    public boolean exists(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        File file = fileCache.get(lowerCaseName);
        return file != null && file.exists();
    }

    public List<File> getAllFiles() {
        return new ArrayList<>(fileCache.values());
    }

    public List<String> getAllCachedPaths() {
        return new ArrayList<>(fileCache.keySet());
    }
}
