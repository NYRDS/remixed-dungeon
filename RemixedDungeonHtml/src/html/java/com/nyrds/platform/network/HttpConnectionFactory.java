package com.nyrds.platform.network;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTML version of HttpConnectionFactory
 */
public class HttpConnectionFactory {
    
    public static HttpURLConnection createConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        return (HttpURLConnection) url.openConnection();
    }
    
    public static HttpURLConnection create(URL url) throws Exception {
        return (HttpURLConnection) url.openConnection();
    }
}