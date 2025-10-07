package com.nyrds.util;

public class DownloadTask extends ParallelDownloadTask {

    public DownloadTask(DownloadStateListener listener, String url, String downloadTo) {
        super(listener, new String[]{url}, downloadTo);

    }
}