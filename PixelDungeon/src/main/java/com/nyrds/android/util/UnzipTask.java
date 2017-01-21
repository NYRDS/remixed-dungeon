package com.nyrds.android.util;

import android.os.AsyncTask;

import java.io.File;

public class UnzipTask extends AsyncTask<String, Integer, Boolean> {

	private UnzipStateListener m_listener;

	public UnzipTask(UnzipStateListener listener) {
		m_listener = listener;
	}

	@Override
	protected Boolean doInBackground(String... args) {

		String zipFile = args[0];

		String tmpDirName = "tmp";

		File tmpDirFile = FileSystem.getExternalStorageFile(tmpDirName);
		if (tmpDirFile.exists()) {
			tmpDirFile.delete();
		}

		if (Unzip.unzip(zipFile, FileSystem.getExternalStorageFile(tmpDirName).getAbsolutePath())) {

			File[] unpackedList = tmpDirFile.listFiles();

			for (File file : unpackedList) {
				if (file.isDirectory()) {

					String modDir = zipFile.substring(0, zipFile.length() - 4);

					if (file.renameTo(new File(modDir))) {
						FileSystem.deleteRecursive(tmpDirFile);
						FileSystem.deleteRecursive(new File(zipFile));
						break;
					} else {
						return false;
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	protected void onProgressUpdate(Integer... progress) {
	}

	protected void onPostExecute(Boolean result) {
		m_listener.UnzipComplete(result);
	}

}