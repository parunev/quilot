package com.quilot.exceptions;

/**
 * Base exception for all errors originating from the AI service module.
 * <p>
 * Using a common base exception allows higher-level error handlers to catch
 * any AI-related issue with a single catch block.
 */
public class AIException extends RuntimeException {

    public AIException(String message) {
        super(message);
    }

    public AIException(String message, Throwable cause) {
        super(message, cause);
    }
}
