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
            DocumentFile file = DocumentFile.fromTreeUri(FileSystem.getContext(), parentDirectoryUri);

            String modPath = parentDirectoryUri.getLastPathSegment();
            String[] parts = modPath.split("/");

            copyDirToAppStorage(file, "", parts[parts.length - 1]);

        } catch (Exception e) {
            e.printStackTrace();
        } finally  {
            mBasePath = null;
            if (mListener!= null)  {
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

        File outputFile = FileSystem.getExternalStorageFile(rootPath + File.separator + pathPrefix + File.separator + file.getName());

        if (outputFile.exists()) {
            if (outputFile.lastModified() > file.lastModified()) {
                return;
            }
        }

        if (mListener != null) {
            mListener.onFileCopy(pathPrefix + File.separator + file.getName());
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
