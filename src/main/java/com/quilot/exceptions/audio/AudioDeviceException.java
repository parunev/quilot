package com.quilot.exceptions.audio;

/**
 * Thrown when an audio device cannot be found, opened, or configured.
 */
public class AudioDeviceException extends AudioException {
    public AudioDeviceException(String message) {
        super(message);
    }

    public AudioDeviceException(String message, Throwable cause) {
        super(message, cause);
    }
}