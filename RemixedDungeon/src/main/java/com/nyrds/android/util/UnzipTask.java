package com.nyrds.android.util;

import java.io.File;

public class UnzipTask implements Runnable {

	private UnzipStateListener m_listener;
	private String m_zipFile;

	public UnzipTask(UnzipStateListener listener, String zipFile) {
		m_listener = listener;
		m_zipFile = zipFile;
	}

	@Override
	public void run() {
		String tmpDirName = "tmp";

		File tmpDirFile = FileSystem.getExternalStorageFile(tmpDirName);
		if (tmpDirFile.exists()) {
			tmpDirFile.delete();
		}

		if (Unzip.unzip(m_zipFile,
				FileSystem.getExternalStorageFile(tmpDirName).getAbsolutePath(),
				unpacked -> m_listener.UnzipProgress(unpacked))) {

			File[] unpackedList = tmpDirFile.listFiles();

			for (File file : unpackedList) {
				if (file.isDirectory()) {

					String modDir = m_zipFile.substring(0, m_zipFile.length() - 4);

					if (file.renameTo(new File(modDir))) {
						FileSystem.deleteRecursive(tmpDirFile);
						FileSystem.deleteRecursive(new File(m_zipFile));
						break;
					} else {
						m_listener.UnzipComplete(false);
					}
				}
			}
			m_listener.UnzipComplete(true);
		} else {
			m_listener.UnzipComplete(false);
		}
	}
}