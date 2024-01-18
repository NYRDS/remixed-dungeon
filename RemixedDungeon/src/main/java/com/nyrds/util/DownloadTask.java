package com.nyrds.util;

import com.nyrds.platform.EventCollector;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import info.guardianproject.netcipher.NetCipher;

public class DownloadTask implements Runnable {

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
            URL url = new URL(m_url);
            File file = new File(m_downloadTo);

            HttpURLConnection ucon = NetCipher.getCompatibleHttpURLConnection(url);

            //ucon.setSSLSocketFactory((SSLSocketFactory) SSLCertificateSocketFactory.getDefault());

            ucon.setReadTimeout(10000);
            ucon.setInstanceFollowRedirects(true);
            ucon.connect();

            int repCode = ucon.getResponseCode();

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

                result = true;
            }

        } catch (Exception e) {
            EventCollector.logException(new ModError("Downloading",e));
        }

         m_listener.DownloadComplete(m_url, result);
    }
}