package com.nyrds.platform.network;

import java.net.HttpURLConnection;
import java.net.URL;

import lombok.SneakyThrows;

public class HttpConnectionFactory {
    @SneakyThrows
    public static HttpURLConnection create(URL url) {
        return (HttpURLConnection) url.openConnection();
    }
}
