/*
 * TeaVM supplemental classlib - minimal implementation for missing JDK classes
 */
package org.teavm.classlib.java.net;

import java.io.IOException;
import java.io.Serializable;

public class TInetAddress implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String hostName;
    private final byte[] address;

    private TInetAddress(String hostName, byte[] address) {
        this.hostName = hostName;
        this.address = address;
    }

    public static TInetAddress getByName(String host) throws TUnknownHostException {
        if (host == null) throw new TUnknownHostException("null host");
        if (host.equals("localhost") || host.equals("127.0.0.1")) {
            return new TInetAddress("localhost", new byte[]{127, 0, 0, 1});
        }
        return new TInetAddress(host, new byte[]{0, 0, 0, 0});
    }

    public static TInetAddress getLocalHost() throws TUnknownHostException {
        return getByName("localhost");
    }

    public static TInetAddress[] getAllByName(String host) throws TUnknownHostException {
        return new TInetAddress[]{getByName(host)};
    }

    public String getHostName() {
        return hostName;
    }

    public byte[] getAddress() {
        return address.clone();
    }

    public String getHostAddress() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < address.length; i++) {
            if (i > 0) sb.append('.');
            sb.append(address[i] & 0xFF);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TInetAddress that = (TInetAddress) obj;
        return hostName.equals(that.hostName);
    }

    @Override
    public int hashCode() {
        return hostName.hashCode();
    }

    @Override
    public String toString() {
        return hostName + "/" + getHostAddress();
    }

    public boolean isLoopbackAddress() {
        if (address.length == 4) {
            return address[0] == 127;
        } else if (address.length == 16) {
            // IPv6 loopback: ::1
            for (int i = 0; i < 15; i++) {
                if (address[i] != 0) return false;
            }
            return address[15] == 1;
        }
        return false;
    }
}