package com.nyrds.platform.storage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FileSystem {
    public static boolean deleteFile(String fileName) {
        try {
            FileHandle file = Gdx.files.local(fileName);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            // File operations are limited in HTML
        }
        return false;
    }

    public static boolean copyFile(String src, String dst) {
        try {
            FileHandle srcFile = Gdx.files.local(src);
            FileHandle dstFile = Gdx.files.local(dst);
            
            if (srcFile.exists()) {
                srcFile.copyTo(dstFile);
                return true;
            }
        } catch (Exception e) {
            // File operations are limited in HTML
        }
        return false;
    }

    public static boolean moveFile(String src, String dst) {
        try {
            FileHandle srcFile = Gdx.files.local(src);
            FileHandle dstFile = Gdx.files.local(dst);
            
            if (srcFile.exists()) {
                srcFile.moveTo(dstFile);
                return true;
            }
        } catch (Exception e) {
            // File operations are limited in HTML
        }
        return false;
    }

    public static boolean fileExists(String fileName) {
        try {
            FileHandle file = Gdx.files.local(fileName);
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    public static long getFileSize(String fileName) {
        try {
            FileHandle file = Gdx.files.local(fileName);
            if (file.exists()) {
                return file.length();
            }
        } catch (Exception e) {
            // File operations are limited in HTML
        }
        return 0;
    }

    public static List<String> listFiles(String dir) {
        List<String> files = new ArrayList<>();
        try {
            FileHandle directory = Gdx.files.local(dir);
            if (directory.exists() && directory.isDirectory()) {
                for (FileHandle file : directory.list()) {
                    files.add(file.name());
                }
            }
        } catch (Exception e) {
            // File operations are limited in HTML
        }
        return files;
    }

    public static InputStream openFileInput(String fileName) {
        try {
            FileHandle file = Gdx.files.local(fileName);
            if (file.exists()) {
                return file.read();
            }
        } catch (Exception e) {
            // File operations are limited in HTML
        }
        return null;
    }

    public static OutputStream openFileOutput(String fileName) {
        try {
            FileHandle file = Gdx.files.local(fileName);
            return file.write(false);
        } catch (Exception e) {
            // File operations are limited in HTML
        }
        return null;
    }
    
    // Additional methods needed for HTML version
    static public FileHandle getInternalStorageFileHandle(String fileName) {
        FileHandle fileHandle = Gdx.files.internal(fileName);
        return fileHandle;
    }
    
    static public boolean exists(String fileName) {
        return getInternalStorageFileHandle(fileName).exists();
    }
    
    static public boolean existsInMod(String fileName) {
        return getInternalStorageFileHandle(fileName).exists();
    }
    
    static public File getInternalStorageFile(String fileName) {
        // In HTML version, we return a File object representing the path
        return new File(Gdx.files.local(fileName).path());
    }
    
    public static File[] listExternalStorage() {
        // In HTML version, we don't have external storage
        return new File[0];
    }
    
    public static File getExternalStorageFile(String fileName) {
        // In HTML version, we use local storage
        return new File(Gdx.files.local(fileName).path());
    }
    
    static public InputStream getInputStream(String filename) {
        try {
            FileHandle file = Gdx.files.local(filename);
            if (file.exists()) {
                return file.read();
            }
        } catch (Exception e) {
            // File operations are limited in HTML
        }
        return null;
    }
    
    public static String getExternalStorageFileName(String fileName) {
        // In HTML version, we use local storage
        return Gdx.files.local(fileName).path();
    }
    
    public static void ensureDir(String dir) {
        // In HTML version, directories are created automatically
        try {
            FileHandle directory = Gdx.files.local(dir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
        } catch (Exception e) {
            // File operations are limited in HTML
        }
    }
    
    public static void deleteRecursive(File file) {
        // In HTML version, we can't delete files recursively
        try {
            if (file != null) {
                FileHandle fileHandle = Gdx.files.absolute(file.getAbsolutePath());
                fileHandle.delete();
            }
        } catch (Exception e) {
            // File operations are limited in HTML
        }
    }
    
    public static OutputStream getOutputStream(String fileName) {
        return openFileOutput(fileName);
    }
    
    // Method needed for mod exporting
    public static void zipFolderTo(OutputStream outputStream, File file, int compressionLevel, Predicate<File> filter) throws IOException {
        // In HTML version, zipping folders is not supported
        System.out.println("Zipping folders not supported in HTML version");
        throw new IOException("Zipping folders not supported in HTML version");
    }
}