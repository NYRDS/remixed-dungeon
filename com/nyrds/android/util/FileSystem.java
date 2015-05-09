package com.nyrds.android.util;

import java.io.File;

import android.content.Context;

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
}
