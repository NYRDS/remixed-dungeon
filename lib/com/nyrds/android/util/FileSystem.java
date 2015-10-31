package com.nyrds.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.util.Log;

public class FileSystem {

	static private Context m_context;

	static public void setContext(Context context) {
		m_context = context;
	}

	static public File getInteralStorageFile(String fileName) {
		File storageDir = m_context.getFilesDir();
		File file = new File(storageDir, fileName);
		return file;
	}

	static public String[] listInternalStorage() {
		File storageDir = m_context.getFilesDir();
		return storageDir.list();
	}

	static public String getInteralStorageFileName(String fileName) {
		return getInteralStorageFile(fileName).getAbsolutePath();
	}

	static public File getExternalStorageFile(String fileName) {
		File storageDir = m_context.getExternalFilesDir(null);
		File file = new File(storageDir, fileName);
		return file;
	}

	static public String getExternalStorageFileName(String fname) {
		return getExternalStorageFile(fname).getAbsolutePath();
	}

	static public File getFile(String fname) {
		if (ModdingMode.mode()) {
			File ret = getExternalStorageFile(fname);
			if (ret.exists()) {
				return ret;
			}
		}
		return getInteralStorageFile(fname);
	}

	static public void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteRecursive(child);

		fileOrDirectory.delete();
	}

	static public void copyFile(String inputFile, String outputFile) {

		try {
			File dir = new File(outputFile).getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}

			InputStream in = new FileInputStream(inputFile);
			OutputStream out = new FileOutputStream(outputFile);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();

			out.flush();
			out.close();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
