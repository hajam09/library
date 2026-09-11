package com.solirius.advanced.library.exceptions;

/**
 * Exception thrown when attempting to add a book that already exists
 * in the library.
 */
public class DuplicateBookException extends Exception {
    /**
     * Constructs a new DuplicateBookException with the specified message.
     *
     * @param message the detail message
     */
    public DuplicateBookException(final String message) {
        super(message);
    }
}
