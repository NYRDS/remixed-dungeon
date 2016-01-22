package com.nyrds.android.util;

public interface DownloadStateListener {
	public void DownloadProgress(String file, Integer percent);
	public void DownloadComplete(String file, Boolean result);
}
