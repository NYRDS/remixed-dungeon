package com.nyrds.android.util;

import java.io.File;
import java.io.FileInputStream;

import lombok.SneakyThrows;

public class UnzipTask implements Runnable {

	private final UnzipStateListener m_listener;
	private final String m_zipFile;
	static private final String tmpDirName = "tmp";


	public UnzipTask(UnzipStateListener listener, String zipFile) {
		m_listener = listener;
		m_zipFile = zipFile;
	}


	@SneakyThrows
	public Mods.ModDesc previewMod() {
		return Unzip.inspectMod(new FileInputStream(m_zipFile),
				FileSystem.getExternalStorageFile(tmpDirName).getAbsolutePath());
	}

	@Override
	public void run() {

		File tmpDirFile = FileSystem.getExternalStorageFile(tmpDirName);
		if (tmpDirFile.exists()) {
			tmpDirFile.delete();
		}

		if (Unzip.unzip(m_zipFile,
				FileSystem.getExternalStorageFile(tmpDirName).getAbsolutePath(),
				m_listener::UnzipProgress)) {

			File[] unpackedList = tmpDirFile.listFiles();

			for (File file : unpackedList) {
				if (file.isDirectory()) {

					File zipFile = new File(m_zipFile);
					String modDir = FileSystem.getExternalStorageFileName(zipFile.getName().split("\\.")[0]);

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