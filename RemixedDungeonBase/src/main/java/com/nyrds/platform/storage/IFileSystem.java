package com.nyrds.platform.storage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public interface IFileSystem {
    // Move all static methods and members here
    @NotNull
    File getInternalStorageFile(String fileName);

    String[] listInternalStorage();

    @NotNull
    File[] listExternalStorage();

    OutputStream getOutputStream(String filename) throws FileNotFoundException;

    InputStream getInputStream(String filename) throws FileNotFoundException;

    String getInternalStorageFileName(String fileName);

    File getExternalStorageFile(String fileName);

    String getExternalStorageFileName(String fname);

    File getFile(String fname);

    void deleteRecursive(File fileOrDirectory);

    void copyStream(InputStream in, OutputStream out) throws IOException;

    void copyFile(String inputFile, OutputStream out) throws IOException;

    void copyFile(String inputFile, String outputFile) throws IOException;

    Map<String, Long> getFileTimestampMap(File directory, String pathPrefix);

    void zipFolderTo(OutputStream out, File srcFolder, int depth, FileFilter filter) throws IOException;

    default void addFolderToZip(File rootFolder, File srcFolder, int depth, ZipOutputStream zip, FileFilter filter) throws IOException {
        for (File file : srcFolder.listFiles(filter)) {
            if (file.isFile()) {
                addFileToZip(rootFolder, file, zip);
                continue;
            }
            if (depth > 0 && file.isDirectory()) {
                addFolderToZip(rootFolder, file, depth - 1, zip, filter);
            }
        }
    }

    default void addFileToZip(File rootFolder, File file, ZipOutputStream zip) throws IOException {
        byte[] buf = new byte[4096];
        int len;
        try (FileInputStream in = new FileInputStream(file)) {
            zip.putNextEntry(new ZipEntry(getRelativePath(file, rootFolder)));
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
            zip.closeEntry();
        }
    }

    String getRelativePath(File file, File folder);
    void ensureDir(String dir) throws RuntimeException;
}
