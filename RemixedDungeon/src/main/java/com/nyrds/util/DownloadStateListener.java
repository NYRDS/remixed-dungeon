package com.nyrds.util;

public interface DownloadStateListener {
	void DownloadProgress(String file, Integer bytes);
	void DownloadComplete(String file, Boolean result);

	interface IDownloadComplete{
		void DownloadComplete(String file, Boolean result);
	}
}
