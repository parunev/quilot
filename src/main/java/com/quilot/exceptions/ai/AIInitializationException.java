package com.quilot.exceptions.ai;

/**
 * Thrown when there is a failure during the initialization of an AI client,
 * such as reading credentials or configuring the service.
 */
public class AIInitializationException extends AIException {

    public AIInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}