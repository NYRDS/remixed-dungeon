package com.nyrds.platform.storage;

import android.content.Context;
import android.net.Uri;
import android.util.Pair;

import androidx.documentfile.provider.DocumentFile;

import com.nyrds.platform.EventCollector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    static public boolean isAutoSyncMaybeNeeded(String modName) {
        File uriFile= FileSystem.getExternalStorageFile(modName + File.separator+"src_uri.sync");
        if (!uriFile.exists()) {
            return false;
        }
        InputStream fis = new FileInputStream(uriFile);
        //read uri from file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line = reader.readLine();
            Uri uri = Uri.parse(line);
            pickModDirectory(uri);
        }
        return true;
    }
    
    static private  void copyModToAppStorage(Context context, Uri parentDirectoryUri) {
        try {
            DocumentFile file = DocumentFile.fromTreeUri(context, parentDirectoryUri);

            String modPath = parentDirectoryUri.getLastPathSegment();
            String[] parts = modPath.split("/");

            String modName = parts[parts.length - 1];
            var externalMap = getFileTimestampMap(file, "");
            var internalMap = FileSystem.getFileTimestampMap(FileSystem.getExternalStorageFile(modName), "");


            Map<String, DocumentFile> newerFiles = new HashMap<>();
            //build map of newer external files
            for (val entry : externalMap.entrySet()) {
                if (internalMap.get(entry.getKey()) == null || internalMap.get(entry.getKey()).compareTo(entry.getValue().first) < 0) {
                    newerFiles.put(entry.getKey(), entry.getValue().second);
                }
            }

            for(val entry : newerFiles.entrySet()) {
                copyDocumentToFile(entry.getValue(), entry.getKey(), FileSystem.getExternalStorageFile(modName + File.separator + entry.getKey()));
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
                if (mListener != null) {
                    mListener.onFileDelete(entry);
                }
            }

            //copyDirToAppStorage(file, "", modName);

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

        copyDocumentToFile(file, filePath, outputFile);
    }

    private static void copyDocumentToFile(DocumentFile file, String filePath, File outputFile) throws FileNotFoundException {
        if (mListener != null) {
            mListener.onFileCopy(filePath);
        }

        File dir = outputFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        InputStream inputStream = FileSystem.getContext().getContentResolver().openInputStream(file.getUri());
        FileSystem.copyStream(inputStream, new FileOutputStream(outputFile));
    }

    public static Map<String, Pair<Long,DocumentFile>> getFileTimestampMap(DocumentFile directory, String pathPrefix) {
        Map<String, Pair<Long,DocumentFile>> fileTimestampMap = new HashMap<>();

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
                        fileTimestampMap.put(file.getName(), new Pair<>(file.lastModified(), file));
                    } else {
                        fileTimestampMap.put(pathPrefix + File.separator + file.getName(), new Pair<>(file.lastModified(), file));
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

        void onFileDelete(String entry);
    }
}
