package com.nyrds.platform.util;

/**
 * HTML version of TrackedRuntimeException
 */
public class TrackedRuntimeException extends RuntimeException {
    
    public TrackedRuntimeException(String message) {
        super(message);
    }
    
    public TrackedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TrackedRuntimeException(Throwable cause) {
        super(cause);
    }
}