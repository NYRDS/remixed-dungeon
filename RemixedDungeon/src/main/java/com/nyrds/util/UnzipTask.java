package com.nyrds.util;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;
import com.watabou.pixeldungeon.utils.Utils;

import java.io.File;

import lombok.Setter;

public class UnzipTask implements Runnable {

	private final UnzipStateListener m_listener;
	private final String m_zipFile;
	static private final String tmpDirName = "tmp";
	private final boolean m_deleteSrc;

	@Setter
	private String tgtDir = Utils.EMPTY_STRING;

	public UnzipTask(UnzipStateListener listener, String zipFile, boolean deleteSrc) {
		m_listener = listener;
		m_zipFile = zipFile;
		m_deleteSrc = deleteSrc;
	}

	@Override
	public void run() {
		try {
			File tmpDirFile = FileSystem.getExternalStorageFile(tmpDirName);
			if (tmpDirFile.exists()) {
				FileSystem.deleteRecursive(tmpDirFile);
			}

			if (Unzip.unzip(m_zipFile,
					FileSystem.getExternalStorageFile(tmpDirName).getAbsolutePath(),
					m_listener)) {

				File[] unpackedList = tmpDirFile.listFiles();
				File zipFile = new File(m_zipFile);

				if (tgtDir.isEmpty()) {
					final String zipFileName = zipFile.getName();

					if(zipFileName.contains(".")) {
						tgtDir = zipFileName.substring(0, zipFileName.lastIndexOf('.'));
					} else {
						tgtDir = zipFileName;
					}
				}

				for (File file : unpackedList) {
					if (file.isDirectory()) {
						if (file.renameTo(new File(FileSystem.getExternalStorageFileName(tgtDir)))) {
							FileSystem.deleteRecursive(tmpDirFile);
							if (m_deleteSrc) {
								FileSystem.deleteRecursive(zipFile);
							}
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
		} catch (Throwable e) {
			EventCollector.logException(e);
		}
	}
}