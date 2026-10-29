package de.office.dashboard.common.exception;

/**
 * Signals that a request collides with a resource that already exists.
 * <p>
 * Typical cases are a lunch event for a date that is already taken, a food
 * catalog item whose label is already in use, or a meal option that is already
 * part of an event. The request itself is well formed, so the answer is a
 * conflict rather than a validation error: {@code GlobalExceptionHandler} maps
 * this exception to HTTP 409 Conflict, which is what the controllers document
 * and what the frontend already checks for.
 * </p>
 */
public class DuplicateResourceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message human readable reason that is returned to the client
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
