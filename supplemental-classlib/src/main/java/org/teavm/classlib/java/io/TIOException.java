/*
 * TeaVM supplemental classlib - minimal implementation for missing JDK classes
 */
package org.teavm.classlib.java.io;

import org.teavm.classlib.java.lang.TException;

public class TIOException extends TException {
    private static final long serialVersionUID = 1L;

    public TIOException() {
    }

    public TIOException(String message) {
        super(message);
    }
}