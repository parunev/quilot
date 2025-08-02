package com.quilot.exceptions.audio;

/**
 * Base exception for all checked errors originating from the audio services module.
 * This class serves as the parent for more specific audio-related exceptions,
 * allowing callers to catch any audio issue with a single catch block.
 */
public class AudioException extends Exception {
    public AudioException(String message) {
        super(message);
    }

    public AudioException(String message, Throwable cause) {
        super(message, cause);
    }
}
