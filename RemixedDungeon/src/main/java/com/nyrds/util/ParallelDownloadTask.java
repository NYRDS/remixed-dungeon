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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ParallelDownloadTask implements Runnable {

    private static final String MAIN_HOST = "https://nyrds.net/";
    private static final String RU_MIRROR_HOST = "https://ru.nyrds.net/";

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

        List<String> attempts = new ArrayList<>();
        attempts.add(url);
        attempts.addAll(mirrorUrl(url));

        for (String attempt : attempts) {
            if (downloadCompleted.get()) {
                return;
            }
            if (downloadFrom(attempt, index)) {
                return;
            }
        }
    }

    // The two mod hosts serve identical paths; ru.nyrds.net has stricter TLS and
    // rejects old clients (tlsv1 alert protocol version), so retry on the sibling
    private static List<String> mirrorUrl(String url) {
        List<String> mirror = new ArrayList<>();
        if (url.startsWith(MAIN_HOST)) {
            mirror.add(RU_MIRROR_HOST + url.substring(MAIN_HOST.length()));
        } else if (url.startsWith(RU_MIRROR_HOST)) {
            mirror.add(MAIN_HOST + url.substring(RU_MIRROR_HOST.length()));
        }
        return mirror;
    }

    private boolean downloadFrom(String url, int index) {
        // If another download already completed, skip this one
        if (downloadCompleted.get()) {
            return true;
        }

        try {
            URL urlObj = new URL(url);
            File file = new File(m_downloadTo + ".tmp" + index);

            HttpURLConnection ucon;

            ucon = HttpConnectionFactory.create(urlObj);

            if (ucon instanceof HttpsURLConnection) {
                trustAllHosts((HttpsURLConnection) ucon);
            }

            ucon.setReadTimeout(30000);
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
                return true;
            } else {
                GLog.debug("Failed to download from " + url + ", response code: " + repCode);
            }

        } catch (Exception e) {
            GLog.debug("Exception downloading from " + url + ": " + e.getMessage());
            EventCollector.logException(new ModError("Downloading from " + url, e));
        }
        return false;
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