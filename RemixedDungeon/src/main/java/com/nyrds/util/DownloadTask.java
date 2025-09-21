package com.nyrds.util;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.network.HttpConnectionFactory;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask implements Runnable {

    private static final int MAX_REDIRECTS = 5;
    
    private final DownloadStateListener m_listener;
    private final String                m_url;
    private final String                m_downloadTo;

    public DownloadTask(DownloadStateListener listener, String url, String downloadTo) {
        m_listener = listener;
        m_url = url;
        m_downloadTo = downloadTo;
    }

    @Override
    public void run() {
        boolean result = false;

        m_listener.DownloadProgress(m_url, 0);

        try {
            result = downloadFile(m_url, m_downloadTo, 0);
        } catch (Exception e) {
            EventCollector.logException(new ModError("Downloading", e));
        }

        m_listener.DownloadComplete(m_url, result);
    }

    private boolean downloadFile(String urlStr, String downloadTo, int redirectCount) throws Exception {
        if (redirectCount > MAX_REDIRECTS) {
            GLog.debug("Too many redirects, aborting download");
            return false;
        }

        URL url = new URL(urlStr);
        File file = new File(downloadTo);

        HttpURLConnection ucon = HttpConnectionFactory.create(url);
        ucon.setReadTimeout(10000);
        ucon.setInstanceFollowRedirects(true);
        ucon.setConnectTimeout(10000);
        ucon.connect();

        int repCode = ucon.getResponseCode();
        GLog.debug("HTTP response code: " + repCode + " for URL: " + urlStr);

        // Handle redirects manually if needed
        if (repCode == HttpURLConnection.HTTP_MOVED_PERM || 
            repCode == HttpURLConnection.HTTP_MOVED_TEMP || 
            repCode == HttpURLConnection.HTTP_SEE_OTHER ||
            repCode == 307 || // TEMPORARY_REDIRECT
            repCode == 308) { // PERMANENT_REDIRECT
            
            String redirectUrl = ucon.getHeaderField("Location");
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                GLog.debug("Following redirect to: " + redirectUrl);
                ucon.disconnect();
                return downloadFile(redirectUrl, downloadTo, redirectCount + 1);
            }
        }

        if (repCode == HttpURLConnection.HTTP_OK) {
            m_listener.DownloadProgress(m_url, 0);
            int bytesTotal = ucon.getContentLength();

            GLog.debug("bytes in file: " + bytesTotal);

            try (InputStream is = ucon.getInputStream();
                 FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024 * 128];
                int count;
                int bytesDownloaded = 0;

                while ((count = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                    bytesDownloaded += count;
                    m_listener.DownloadProgress(m_url, bytesDownloaded);
                    Thread.yield();
                }
            }

            ucon.disconnect();
            return true;
        } else {
            GLog.debug("Download failed with HTTP response code: " + repCode);
            ucon.disconnect();
            return false;
        }
    }
}