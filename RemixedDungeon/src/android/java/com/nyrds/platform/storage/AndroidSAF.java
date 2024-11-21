package com.nyrds.platform.storage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.util.Pair;

import androidx.documentfile.provider.DocumentFile;

import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.events.EventCollector;
import com.nyrds.platform.game.Game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.SneakyThrows;
import lombok.val;

public class AndroidSAF {
    public static Uri mBaseSrcPath = null;
    public static Uri mBaseDstPath = null;
    static IListener mListener;

    public static void copyModToAppStorage() {
        if (mBaseSrcPath == null) {
            return;
        }
        copyModToAppStorage(RemixedDungeonApp.getContext(), mBaseSrcPath);
    }

    public static void pickModSourceDirectory(Uri selectedDirectoryUri) {
        mBaseSrcPath = selectedDirectoryUri;
        mBaseDstPath = null;
    }

    public static void pickModDstDirectory(Uri selectedDirectoryUri) {
        mBaseDstPath = selectedDirectoryUri;
        mBaseSrcPath = null;
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
            pickModSourceDirectory(uri);
        }
        return true;
    }
    
    static private  void copyModToAppStorage(Context context, Uri parentDirectoryUri) {
        try {

            if (mListener != null) {
                mListener.onMessage("Syncing mod with app storage...");
            }

            DocumentFile file = DocumentFile.fromTreeUri(context, parentDirectoryUri);

            String modPath = parentDirectoryUri.getLastPathSegment();
            String[] parts = modPath.split("/");

            String modName = parts[parts.length - 1];


            if (mListener != null) {
                mListener.onMessage("Building SAF timestamp map...");
            }

            var externalMap = getFileTimestampMap(file, "");

            if (mListener != null) {
                mListener.onMessage("Building app storage timestamp map...");
            }

            var internalMap = FileSystem.getFileTimestampMap(FileSystem.getExternalStorageFile(modName), "");

            if (mListener != null) {
                mListener.onMessage("Finding newer files...");
            }

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

            if (mListener != null) {
                mListener.onMessage("Finding deleted files...");
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

            if (mListener != null) {
                mListener.onMessage("Saving sync uri...");
            }

            File uri = FileSystem.getExternalStorageFile(modName + File.separator+"src_uri.sync");
            FileOutputStream fos = new FileOutputStream(uri);
            fos.write(parentDirectoryUri.toString().getBytes());
            fos.close();

        } catch (Exception e) {
            EventCollector.logException(e, "copyModToAppStorage");
        } finally {
            mBaseSrcPath = null;
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

        InputStream inputStream = RemixedDungeonApp.getContext().getContentResolver().openInputStream(file.getUri());
        FileSystem.copyStream(inputStream, new FileOutputStream(outputFile));
    }

    public static OutputStream outputStreamToDocument(Context context, Uri directoryUri, String fileName) throws IOException {
        DocumentFile directory = DocumentFile.fromTreeUri(context, directoryUri);
        if (directory != null && directory.isDirectory()) {
            DocumentFile newFile = directory.createFile("application/octet-stream", fileName);
            if (newFile != null) {
                Log.v("AndroidSAF", "Created new document in " + directoryUri + " with name " + fileName);
                return context.getContentResolver().openOutputStream(newFile.getUri());
            } else {
                Log.e("AndroidSAF", "Failed to create new document in " + directoryUri + " with name " + fileName);
            }
        } else {
            Log.e("AndroidSAF", "Uri " + directoryUri + " doesn't point to a directory");
        }
        throw new IOException("Failed to create new document in " + directoryUri);
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

    static public void pickDirectoryForModInstall() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory());
        Game.instance().startActivityForResult(intent, Game.REQUEST_CODE_OPEN_DOCUMENT_TREE_MOD_DIR_INSTALL);
    }

    static public void pickDirectoryForModExport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory());
        Game.instance().startActivityForResult(intent, Game.REQUEST_CODE_OPEN_DOCUMENT_TREE_MOD_DIR_EXPORT);
    }

    public interface IListener {
        void onMessage(String message);
        void onFileCopy(String path);

        void onFileSkip(String path);

        void onComplete();

        void onFileDelete(String entry);
    }
}
