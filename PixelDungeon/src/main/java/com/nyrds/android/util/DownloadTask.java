package com.nyrds.android.util;

import android.os.AsyncTask;
import android.util.Log;

import com.nyrds.retrodungeon.ml.EventCollector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Integer, Boolean> {

	private static final String TAG = "DownloadTask";
	private DownloadStateListener m_listener;
	private String                m_url;
	
	public DownloadTask(DownloadStateListener listener) {
		m_listener = listener;
	}

	@Override
	protected Boolean doInBackground(String... args) {
		Boolean result = false;
		m_url = args[0];
		
		System.setProperty("https.protocols", "TLSv1");
		
		publishProgress( 0 );
		
		try {
			URL url   = new URL(m_url);
			File file = new File(args[1]);

			long startTime = System.currentTimeMillis();
			
			Log.d(TAG, "download beginning");
			Log.d(TAG, "download url: " + url);
			Log.d(TAG, "downloaded file name: " + file);
			
			HttpURLConnection ucon = (HttpURLConnection) url.openConnection();

			ucon.setReadTimeout(2500);
			ucon.setInstanceFollowRedirects(true);
			ucon.connect();

			int repCode = ucon.getResponseCode();
			
			if (repCode == HttpURLConnection.HTTP_OK) {
				int bytesTotal = ucon.getContentLength();

				Log.d(TAG, "bytes in file: " + bytesTotal);

				InputStream is = ucon.getInputStream();
				
				FileOutputStream fos = new FileOutputStream(file);

				byte buffer[] = new byte[4096];
				int count;
				int bytesDownloaded = 0;
				while ((count = is.read(buffer)) != -1) {
					fos.write(buffer, 0, count);
					bytesDownloaded += count;
					if(bytesTotal > 0){
						publishProgress( (100 * bytesDownloaded) / bytesTotal);
					}
				}

				fos.close();
				publishProgress( 100 );
				Log.d(TAG,
						"download ready in: "
								+ ((System.currentTimeMillis() - startTime) / 1000)
								+ " sec");
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
    	if(result){
    		Log.d(TAG, "Download ok");
    	} else {
    		Log.d(TAG, "Download failed");
    	}
    	m_listener.DownloadComplete(m_url, result);
    }

}