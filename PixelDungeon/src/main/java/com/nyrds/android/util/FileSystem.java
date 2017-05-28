package com.nyrds.android.util;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileSystem {

	static private Context m_context;

	static public void setContext(Context context) {
		m_context = context;
	}

	static public File getInternalStorageFile(String fileName) {
		File storageDir = m_context.getFilesDir();
		return new File(storageDir, fileName);
	}

	static public String[] listInternalStorage() {
		File storageDir = m_context.getFilesDir();
		return storageDir.list();
	}

	@NonNull
	static public File[] listExternalStorage() {

		File storageDir = m_context.getExternalFilesDir(null);
		if (storageDir != null) {
			File[] ret = storageDir.listFiles();
			if(ret != null) {
				return ret;
			}
		}

		return new File[0];
	}

	static public OutputStream getOutputStream(String filename) throws FileNotFoundException {
		File dir = new File(filename).getParentFile();
		if (dir != null && !dir.exists()) {
			dir.mkdirs();
		}

		return new FileOutputStream(FileSystem.getInternalStorageFile(filename));
	}

	static public InputStream getInputStream(String filename) throws FileNotFoundException {
		return new FileInputStream(FileSystem.getInternalStorageFile(filename));
	}

	static public String getInternalStorageFileName(String fileName) {
		return getInternalStorageFile(fileName).getAbsolutePath();
	}

	static public File getExternalStorageFile(String fileName) {
		File storageDir = m_context.getExternalFilesDir(null);
		return new File(storageDir, fileName);
	}

	static public String getExternalStorageFileName(String fname) {
		return getExternalStorageFile(fname).getAbsolutePath();
	}

	static public File getFile(String fname) {
		return getInternalStorageFile(fname);
	}

	static public void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteRecursive(child);

		fileOrDirectory.delete();
	}

	static public void copyStream(InputStream in, OutputStream out) {
		try {
			byte[] buffer = new byte[4096];
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

	static public void copyFile(String inputFile, OutputStream out) {
		try {
			InputStream in = new FileInputStream(inputFile);

			copyStream(in, out);
		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}


	static public void copyFile(String inputFile, String outputFile) {
		try {
			File dir = new File(outputFile).getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}

			copyFile(inputFile, new FileOutputStream(outputFile));
		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}
}
