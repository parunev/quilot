package com.quilot.exceptions.stt;

/**
 * Thrown specifically when there is an authentication or credentials issue with the STT service.
 */
public class STTAuthenticationException extends STTException {
    public STTAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}