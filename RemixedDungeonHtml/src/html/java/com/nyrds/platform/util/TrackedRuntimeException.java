package com.nyrds.platform.util;

import org.teavm.jso.JSBody;

/**
 * HTML version of TrackedRuntimeException.
 * TeaVM's Throwable(Throwable) does not surface the cause through
 * getMessage(), and browser error events hide the wrapped exception -
 * so log the cause chain into the JS console at construction time.
 */
public class TrackedRuntimeException extends RuntimeException {

    public TrackedRuntimeException(String message) {
        super(message);
        jsLog(message);
    }

    public TrackedRuntimeException(String message, Throwable cause) {
        super(message, cause);
        jsLog(message + " <- " + stackOf(cause));
    }

    public TrackedRuntimeException(Throwable cause) {
        super(cause);
        jsLog(stackOf(cause));
    }

    private static String stackOf(Throwable t) {
        if (t == null) {
            return "null cause";
        }
        StringBuilder sb = new StringBuilder(String.valueOf(t));
        for (StackTraceElement el : t.getStackTrace()) {
            sb.append("\n  at ").append(el);
        }
        Throwable inner = t.getCause();
        if (inner != null) {
            sb.append("\nCAUSED BY ").append(inner);
        }
        return sb.toString();
    }

    @JSBody(params = "text", script = "console.error('JVM-EXCEPTION: ' + text);")
    private static native void jsLog(String text);
}
