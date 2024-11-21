package com.nyrds.platform.storage;

import android.content.Context;

import com.nyrds.platform.IFileSystem;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.util.ModError;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipOutputStream;

public class FileSystemAndroid implements IFileSystem {
    // Move all static methods and members here
    @Override
    public @NotNull File getInternalStorageFile(String fileName) {
        File storageDir = getContext().getFilesDir();
        return new File(storageDir, fileName);
    }

    @Override
    public String[] listInternalStorage() {
        File storageDir = getContext().getFilesDir();
        return storageDir.list();
    }

    @Override
    public @NotNull File[] listExternalStorage() {
        File storageDir = getExternalStorageFile(".");
        if (storageDir != null) {
            File[] ret = storageDir.listFiles();
            if (ret != null) {
                return ret;
            }
        }
        return new File[0];
    }

    @Override
    public OutputStream getOutputStream(String filename) throws FileNotFoundException {
        File dir = new File(filename).getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        return new FileOutputStream(FileSystemAndroid.this.getInternalStorageFile(filename));
    }

    @Override
    public InputStream getInputStream(String filename) throws FileNotFoundException {
        return new FileInputStream(FileSystemAndroid.this.getInternalStorageFile(filename));
    }

    @Override
    public String getInternalStorageFileName(String fileName) {
        return getInternalStorageFile(fileName).getAbsolutePath();
    }

    @Override
    public File getExternalStorageFile(String fileName) {
        File storageDir = getContext().getExternalFilesDir(null);
        return new File(storageDir, fileName);
    }

    @Override
    public String getExternalStorageFileName(String fname) {
        return getExternalStorageFile(fname).getAbsolutePath();
    }

    @Override
    public File getFile(String fname) {
        return getInternalStorageFile(fname);
    }

    @Override
    public void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    @Override
    public void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.flush();
        out.close();
    }

    @Override
    public void copyFile(String inputFile, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(inputFile);
        copyStream(in, out);
    }

    @Override
    public void copyFile(String inputFile, String outputFile) throws IOException {
        File dir = new File(outputFile).getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        copyFile(inputFile, new FileOutputStream(outputFile));
    }

    @Override
    public Map<String, Long> getFileTimestampMap(File directory, String pathPrefix) {
        Map<String, Long> ret = new HashMap<>();
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    ret.putAll(getFileTimestampMap(file, pathPrefix + file.getName() + File.separator));
                } else {
                    ret.put(pathPrefix + file.getName(), file.lastModified());
                }
            }
        }
        return ret;
    }

    @Override
    public void zipFolderTo(OutputStream out, File srcFolder, int depth, FileFilter filter) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(out);
        addFolderToZip(srcFolder, srcFolder, depth, zip, filter);
        zip.flush();
        zip.close();
    }

    @Override
    public String getRelativePath(File file, File folder) {
        String filePath = file.getAbsolutePath();
        String folderPath = folder.getAbsolutePath();
        if (filePath.startsWith(folderPath)) {
            return filePath.substring(folderPath.length() + 1);
        } else {
            return null;
        }
    }

    public Context getContext() {
        return RemixedDungeonApp.getContext();
    }

    @Override
    public void ensureDir(String dir) throws ModError {
        File f = new File(dir);
        if (f.exists() && f.isDirectory()) {
            return;
        }
        if (f.exists() && !f.delete()) {
            throw new ModError("Can't cleanup:" + dir);
        }
        if (!f.mkdirs()) {
            throw new ModError("Can't create directory:" + dir);
        }
    }
}
