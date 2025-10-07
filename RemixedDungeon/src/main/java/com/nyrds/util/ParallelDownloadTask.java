package com.nyrds.util;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.network.HttpConnectionFactory;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ParallelDownloadTask implements Runnable {

    private final DownloadStateListener m_listener;
    private final String[] m_urls;
    private final String m_downloadTo;
    private final AtomicBoolean downloadCompleted = new AtomicBoolean(false);

    public ParallelDownloadTask(DownloadStateListener listener, String[] urls, String downloadTo) {
        m_listener = listener;
        m_urls = urls;
        m_downloadTo = downloadTo;
    }

    @Override
    public void run() {
        // Start all downloads in parallel
        Thread[] downloadThreads = new Thread[m_urls.length];
        
        for (int i = 0; i < m_urls.length; i++) {
            final String url = m_urls[i];
            final int index = i;
            
            downloadThreads[i] = new Thread(() -> {
                downloadFile(url, index);
            });
            downloadThreads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : downloadThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // If no download succeeded, notify failure
        if (!downloadCompleted.get()) {
            m_listener.DownloadComplete("all_failed", false);
        }
    }

    private void downloadFile(String url, int index) {
        // If another download already completed, skip this one
        if (downloadCompleted.get()) {
            return;
        }

        try {
            URL urlObj = new URL(url);
            File file = new File(m_downloadTo + ".tmp" + index);

            HttpURLConnection ucon;

            ucon = HttpConnectionFactory.create(urlObj);

            if (ucon instanceof HttpsURLConnection) {
                trustAllHosts((HttpsURLConnection) ucon);
            }

            ucon.setReadTimeout(10000);
            ucon.setInstanceFollowRedirects(true);
            ucon.connect();

            int repCode = ucon.getResponseCode();

            if (repCode == HttpURLConnection.HTTP_OK) {
                int bytesTotal = ucon.getContentLength();
                GLog.debug("bytes in file from " + url + ": " + bytesTotal);

                try (InputStream is = ucon.getInputStream();
                     FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024 * 128];
                    int count;
                    int bytesDownloaded = 0;

                    while ((count = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, count);
                        bytesDownloaded += count;
                        // Only report progress if this is the first download to finish
                        if (!downloadCompleted.get()) {
                            m_listener.DownloadProgress(url, bytesDownloaded);
                        }
                        Thread.yield();
                    }
                }

                // If this is the first download to complete successfully, mark as completed
                // and move the file to the final destination
                if (!downloadCompleted.getAndSet(true)) {
                    File finalFile = new File(m_downloadTo);
                    if (finalFile.exists()) {
                        finalFile.delete();
                    }
                    file.renameTo(finalFile);
                    
                    // Cancel other downloads by interrupting their threads
                    m_listener.DownloadComplete(url, true);
                } else {
                    // Another download already completed, clean up this file
                    file.delete();
                }
            } else {
                GLog.debug("Failed to download from " + url + ", response code: " + repCode);
            }

        } catch (Exception e) {
            GLog.debug("Exception downloading from " + url + ": " + e.getMessage());
            EventCollector.logException(new ModError("Downloading from " + url, e));
        }
    }

    private static void trustAllHosts(HttpsURLConnection connection) {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                GLog.debug("checkClientTrusted");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                GLog.debug("checkServerTrusted");
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            connection.setSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            GLog.debug("Failed to install trust all certs manager", e);
        }

        connection.setHostnameVerifier((hostname, session) -> true);
    }
}