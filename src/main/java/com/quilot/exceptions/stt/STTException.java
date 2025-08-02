package com.quilot.exceptions.stt;

/**
 * Base exception for all errors originating from the STT service.
 */
public class STTException extends Exception {
    public STTException(String message) {
        super(message);
    }

    public STTException(String message, Throwable cause) {
        super(message, cause);
    }
}