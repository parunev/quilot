package com.quilot.exceptions;

/**
 * Thrown when there is an error loading, saving, or accessing AI settings,
 * often related to issues with the underlying persistence layer (e.g., Java Preferences).
 */
public class AISettingsException extends AIException {

    public AISettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
