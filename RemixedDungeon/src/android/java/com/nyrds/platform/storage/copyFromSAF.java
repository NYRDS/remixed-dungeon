package com.nyrds.platform.storage;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import lombok.SneakyThrows;

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

    static private void copyModToAppStorage(Context context, Uri parentDirectoryUri) {


        try {
            DocumentFile file = DocumentFile.fromTreeUri(context, parentDirectoryUri);

            String modPath = parentDirectoryUri.getLastPathSegment();
            String[] parts = modPath.split("/");

            String modName  = parts[parts.length  -  1];
            var externalMap = getFileTimestampMap(file,"");
            var internalMap = FileSystem.getFileTimestampMap(FileSystem.getExternalStorageFile(modName),"");


            copyDirToAppStorage(file, "", modName);

        } catch (Exception e) {
            e.printStackTrace();
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

    public static void setListener(IListener listener) {
        mListener = listener;
    }

    public interface IListener {
        void onFileCopy(String path);

        void onFileSkip(String path);

        void onComplete();
    }
}
