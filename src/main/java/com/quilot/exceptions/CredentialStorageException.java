package com.quilot.exceptions;

/**
 * Thrown for errors during the saving or loading of credentials.
 */
public class CredentialStorageException extends Exception {
    public CredentialStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
