package uk.gov.register.exceptions;

public class FieldConversionException extends RuntimeException {
    public FieldConversionException(final String message) {
        super(message);
    }
}
