package com.nyrds.platform.storage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import lombok.SneakyThrows;


public class FileSystem {
    private static IFileSystem impl;

    public static void init(IFileSystem impl) {
        FileSystem.impl = impl;
    }

    // Forward all static methods to the FileSystemImpl instance
    public static @NotNull File getInternalStorageFile(String fileName) {
        return FileSystem.impl.getInternalStorageFile(fileName);
    }
    public static String[] listInternalStorage() {
        return FileSystem.impl.listInternalStorage();
    }
    public static @NotNull File[] listExternalStorage() {
        return FileSystem.impl.listExternalStorage();
    }
    public static OutputStream getOutputStream(String filename) throws FileNotFoundException {
        return FileSystem.impl.getOutputStream(filename);
    }
    public static InputStream getInputStream(String filename) throws FileNotFoundException {
        return FileSystem.impl.getInputStream(filename);
    }
    public static String getInternalStorageFileName(String fileName) {
        return FileSystem.impl.getInternalStorageFileName(fileName);
    }
    public static File getExternalStorageFile(String fileName) {
        return FileSystem.impl.getExternalStorageFile(fileName);
    }
    public static String getExternalStorageFileName(String fname) {
        return FileSystem.impl.getExternalStorageFileName(fname);
    }

    @SneakyThrows
    public static File getFile(String fname) {
        return FileSystem.impl.getFile(fname);
    }

    @SneakyThrows
    public static void deleteRecursive(File fileOrDirectory) {
        FileSystem.impl.deleteRecursive(fileOrDirectory);
    }

    @SneakyThrows
    public static void copyStream(InputStream in, OutputStream out) {
        FileSystem.impl.copyStream(in, out);
    }

    @SneakyThrows
    public static void copyFile(String inputFile, OutputStream out) {
        FileSystem.impl.copyFile(inputFile, out);
    }

    @SneakyThrows
    public static void copyFile(String inputFile, String outputFile) {
        FileSystem.impl.copyFile(inputFile, outputFile);
    }
    public static Map<String, Long> getFileTimestampMap(File directory, String pathPrefix) {
        return FileSystem.impl.getFileTimestampMap(directory, pathPrefix);
    }
    public static void zipFolderTo(OutputStream out, File srcFolder, int depth, FileFilter filter) throws IOException {
        FileSystem.impl.zipFolderTo(out, srcFolder, depth, filter);
    }
    public static String getRelativePath(File file, File folder) {
        return FileSystem.impl.getRelativePath(file, folder);
    }
    public static void ensureDir(String dir) throws RuntimeException {
        FileSystem.impl.ensureDir(dir);
    }
}
