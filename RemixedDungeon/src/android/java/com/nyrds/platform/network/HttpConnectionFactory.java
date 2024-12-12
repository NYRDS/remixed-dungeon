package com.nyrds.platform.network;

import java.net.HttpURLConnection;
import java.net.URL;

import info.guardianproject.netcipher.NetCipher;
import lombok.SneakyThrows;

public class HttpConnectionFactory {
    @SneakyThrows
    public static HttpURLConnection create(URL url) {
        return NetCipher.getCompatibleHttpURLConnection(url);
    }
}
