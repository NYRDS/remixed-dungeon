package com.nyrds.android.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileSystem {

	static private Context m_context;

	static public void setContext(Context context) {
		m_context = context;
	}

	static public File getInteralStorageFile(String fileName) {
		File storageDir = m_context.getFilesDir();
		return new File(storageDir, fileName);
	}

	static public String[] listInternalStorage() {
		File storageDir = m_context.getFilesDir();
		return storageDir.list();
	}

	static public File[] listExternalStorage() {
		File storageDir = m_context.getExternalFilesDir(null);
		if (storageDir != null) {
			return storageDir.listFiles();
		} else {
			return new File[0];
		}
	}

	static public String getInteralStorageFileName(String fileName) {
		return getInteralStorageFile(fileName).getAbsolutePath();
	}

	static public File getExternalStorageFile(String fileName) {
		File storageDir = m_context.getExternalFilesDir(null);
		return new File(storageDir, fileName);
	}

	static public String getExternalStorageFileName(String fname) {
		return getExternalStorageFile(fname).getAbsolutePath();
	}

	static public File getFile(String fname) {
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
			throw new TrackedRuntimeException(e);
		}

	}
}
