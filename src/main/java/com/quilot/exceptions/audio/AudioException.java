package com.quilot.exceptions.audio;

/**
 * Base exception for all errors originating from the audio services module.
 */
public class AudioException extends Exception {
    public AudioException(String message) {
        super(message);
    }

    public AudioException(String message, Throwable cause) {
        super(message, cause);
    }
}
