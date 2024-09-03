package com.nyrds.platform.storage;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.nyrds.platform.EventCollector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.SneakyThrows;
import lombok.val;

public class copyFromSAF {
    public static Uri mBasePath;
    static IListener mListener;

    public static void copyModToAppStorage() {
        if (mBasePath == null) {
            return;
        }
        copyModToAppStorage(FileSystem.getContext(), mBasePath);
    }

    public static void pickModDirectory(Uri selectedDirectoryUri) {
        mBasePath = selectedDirectoryUri;
    }

    @SneakyThrows
    static public void autoSyncModDirectory(String modName) {
        File uriFile= FileSystem.getExternalStorageFile(modName + File.separator+"src_uri.sync");
        if (!uriFile.exists()) {
            return;
        }
        InputStream fis = new FileInputStream(uriFile);
        //read uri from file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line = reader.readLine();
            Uri uri = Uri.parse(line);
            pickModDirectory(uri);
            copyModToAppStorage();
        }
    }

    static private  void copyModToAppStorage(Context context, Uri parentDirectoryUri) {
        try {
            DocumentFile file = DocumentFile.fromTreeUri(context, parentDirectoryUri);

            String modPath = parentDirectoryUri.getLastPathSegment();
            String[] parts = modPath.split("/");

            String modName = parts[parts.length - 1];
            var externalMap = getFileTimestampMap(file, "");
            var internalMap = FileSystem.getFileTimestampMap(FileSystem.getExternalStorageFile(modName), "");

            Set<String> newerFiles = new HashSet<>();
            //build map of newer external files
            for (val entry : externalMap.entrySet()) {
                if (internalMap.get(entry.getKey()) == null || internalMap.get(entry.getKey()).compareTo(entry.getValue()) < 0) {
                    newerFiles.add(entry.getKey());
                }
            }

            Set<String> deletedFiles = new HashSet<>();
            //build map of deleted external files
            for (val entry : internalMap.entrySet()) {
                if (externalMap.get(entry.getKey()) == null) {
                    deletedFiles.add(entry.getKey());
                }
            }

            for (val entry : deletedFiles) {
                FileSystem.getExternalStorageFile(modName+File.separator+entry).delete();
            }

            copyDirToAppStorage(file, "", modName);

            File uri = FileSystem.getExternalStorageFile(modName + File.separator+"src_uri.sync");
            FileOutputStream fos = new FileOutputStream(uri);
            fos.write(parentDirectoryUri.toString().getBytes());
            fos.close();

        } catch (Exception e) {
            EventCollector.logException(e, "copyModToAppStorage");
        } finally {
            mBasePath = null;
            if (mListener != null) {
                mListener.onComplete();
            }
        }
    }

    @SneakyThrows
    private static void copyDirToAppStorage(DocumentFile directory, String pathPrefix, String rootPath) {
        DocumentFile[] files = directory.listFiles();
        for (DocumentFile file : files) {
            if (file.isDirectory()) {
                if (pathPrefix.isEmpty()) {
                    copyDirToAppStorage(file, file.getName(), rootPath);
                } else {
                    copyDirToAppStorage(file, pathPrefix + File.separator + file.getName(), rootPath);
                }
            } else {
                copyToAppStorage(pathPrefix, rootPath, file);
            }
        }
    }

    @SneakyThrows
    private static void copyToAppStorage(String pathPrefix, String rootPath, DocumentFile file) {
        InputStream inputStream = FileSystem.getContext().getContentResolver().openInputStream(file.getUri());
        String filePath = pathPrefix + File.separator + file.getName();
        File outputFile = FileSystem.getExternalStorageFile(rootPath + File.separator + filePath);

        if (outputFile.exists()) {
            if (outputFile.lastModified() > file.lastModified()) {
                if (mListener != null) {
                    mListener.onFileSkip(filePath);
                }
                return;
            }
        }

        if (mListener != null) {
            mListener.onFileCopy(filePath);
        }

        File dir = outputFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileSystem.copyStream(inputStream, new FileOutputStream(outputFile));
    }

    public static Map<String, Long> getFileTimestampMap(DocumentFile directory, String pathPrefix) {
        Map<String, Long> fileTimestampMap = new HashMap<>();

        try {
            DocumentFile[] files = directory.listFiles();
            for (DocumentFile file : files) {
                if (file.isDirectory()) {
                    if (pathPrefix.isEmpty()) {
                        val ret = getFileTimestampMap(file, file.getName());
                        fileTimestampMap.putAll(ret);
                    } else {
                        val ret = getFileTimestampMap(file, pathPrefix + File.separator + file.getName());
                        fileTimestampMap.putAll(ret);
                    }
                } else {
                    if (pathPrefix.isEmpty()) {
                        fileTimestampMap.put(file.getName(), file.lastModified());
                    } else {
                        fileTimestampMap.put(pathPrefix + File.separator + file.getName(), file.lastModified());
                    }
                }
            }
        } catch (Exception e) {
            EventCollector.logException(e, "getFileTimestampMap");
        } finally {
            return fileTimestampMap;
        }
    }

    public static void setListener(IListener listener) {
        mListener = listener;
    }

    public interface IListener {
        void onFileCopy(String path);

        void onFileSkip(String path);

        void onComplete();
    }
}
