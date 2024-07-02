package com.nyrds.platform.storage;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import lombok.SneakyThrows;

public class copyFromSAF {
    public static void copyModFromSAF(Uri selectedDirectoryUri) {
        Uri mBasePath = selectedDirectoryUri;
        copyModToAppStorage(FileSystem.getContext(), mBasePath);
    }

    static private void copyModToAppStorage(Context context, Uri parentDirectoryUri) {
        try {
            DocumentFile file = DocumentFile.fromTreeUri(FileSystem.getContext(), parentDirectoryUri);

            String modPath = parentDirectoryUri.getLastPathSegment();
            String[] parts = modPath.split("/");

            copyDirToAppStorage(file, "", parts[parts.length - 1]);

        } catch (Exception e) {
            e.printStackTrace();
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

        File dir = outputFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileSystem.copyStream(inputStream, new FileOutputStream(outputFile));
    }
}
