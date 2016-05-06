package com.nyrds.android.util;

public interface DownloadStateListener {
	void DownloadProgress(String file, Integer percent);
	void DownloadComplete(String file, Boolean result);
}
