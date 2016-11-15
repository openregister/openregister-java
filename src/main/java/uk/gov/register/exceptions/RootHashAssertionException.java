package uk.gov.register.exceptions;

public class RootHashAssertionException extends RuntimeException {

    public RootHashAssertionException(String message) {
        super(message);
    }

    public RootHashAssertionException(String message, Exception e) {
        super(message, e);
    }

}
