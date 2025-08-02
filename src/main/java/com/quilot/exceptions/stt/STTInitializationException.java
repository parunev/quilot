package com.quilot.exceptions.stt;

/**
 * An unchecked exception for critical, unrecoverable failures during STT service initialization.
 */
public class STTInitializationException extends RuntimeException {
    public STTInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
