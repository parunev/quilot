package com.quilot.exceptions.audio;

/**
 * A checked exception thrown when an audio device cannot be found, opened, or configured.
 * This typically occurs if a device is disconnected, in use by another application,
 * or does not support the required audio format.
 */
public class AudioDeviceException extends AudioException {
    public AudioDeviceException(String message) {
        super(message);
    }

    public AudioDeviceException(String message, Throwable cause) {
        super(message, cause);
    }
}