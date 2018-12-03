package com.nyrds.android.util;

import android.os.AsyncTask;
import android.os.Build;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import info.guardianproject.netcipher.NetCipher;

public class DownloadTask extends AsyncTask<String, Integer, Boolean> {

    private DownloadStateListener m_listener;
    private String                m_url;

    public DownloadTask(DownloadStateListener listener) {
        m_listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... args) {
        Boolean result = false;
        m_url = args[0];

        publishProgress(0);

        try {
            URL url = new URL(m_url);
            File file = new File(args[1]);

            EventCollector.startTrace("download");

            HttpsURLConnection ucon = NetCipher.getHttpsURLConnection(url);

            ucon.setReadTimeout(2500);
            ucon.setInstanceFollowRedirects(true);
            ucon.connect();

            int repCode = ucon.getResponseCode();

            if (repCode == HttpURLConnection.HTTP_OK) {
                int bytesTotal = ucon.getContentLength();

                GLog.debug("bytes in file: " + bytesTotal);

                InputStream is = ucon.getInputStream();

                FileOutputStream fos = new FileOutputStream(file);

                byte buffer[] = new byte[16384];
                int count;
                int bytesDownloaded = 0;
                while ((count = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                    bytesDownloaded += count;
                    publishProgress(bytesDownloaded);
                }

                fos.close();
                EventCollector.stopTrace("download", "download", m_url, "");
                publishProgress(100);

                result = true;
            } else {
                result = false;
            }
        } catch (Exception e) {
            EventCollector.logException(e);
        }

        return result;
    }


    protected void onProgressUpdate(Integer... progress) {
        m_listener.DownloadProgress(m_url, progress[0]);
    }

    protected void onPostExecute(Boolean result) {
        m_listener.DownloadComplete(m_url, result);
    }


    public void download(String url, String downloadTo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.executeOnExecutor(Game.instance().executor, url, downloadTo);
        } else {
            this.execute(url, downloadTo);
        }
    }

}