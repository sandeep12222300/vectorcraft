package com.yourownai.exception;

/**
 * Provides ResourceNotFoundException behavior.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Executes ResourceNotFoundException.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
