package uk.gov.register.exceptions;

public class InconsistencyException extends RuntimeException {

    public InconsistencyException(final String message) {
        super(message);
    }
}
