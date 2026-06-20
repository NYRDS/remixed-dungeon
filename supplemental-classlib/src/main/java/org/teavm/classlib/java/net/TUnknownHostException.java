/*
 * TeaVM supplemental classlib - minimal implementation for missing JDK classes
 */
package org.teavm.classlib.java.net;

import org.teavm.classlib.java.io.TIOException;

public class TUnknownHostException extends TIOException {
    private static final long serialVersionUID = 1L;

    public TUnknownHostException(String detailMessage) {
        super(detailMessage);
    }

    public TUnknownHostException() {
    }
}