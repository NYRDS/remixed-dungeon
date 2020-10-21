package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.pixeldungeon.utils.Utils;

import java.io.File;
import java.io.FileInputStream;

import lombok.Setter;
import lombok.SneakyThrows;

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

	@SneakyThrows
	public Mods.ModDesc previewMod() {
		return Unzip.inspectMod(new FileInputStream(m_zipFile));
	}

	@Override
	public void run() {
		try {
			File tmpDirFile = FileSystem.getExternalStorageFile(tmpDirName);
			if (tmpDirFile.exists()) {
				tmpDirFile.delete();
			}

			if (Unzip.unzip(m_zipFile,
					FileSystem.getExternalStorageFile(tmpDirName).getAbsolutePath(),
					m_listener::UnzipProgress)) {

				File[] unpackedList = tmpDirFile.listFiles();

				File zipFile = new File(m_zipFile);

				if (tgtDir.isEmpty()) {
					tgtDir = FileSystem.getExternalStorageFileName(zipFile.getName().split("\\.")[0]);
				}

				for (File file : unpackedList) {
					if (file.isDirectory()) {
						if (file.renameTo(new File(tgtDir))) {
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