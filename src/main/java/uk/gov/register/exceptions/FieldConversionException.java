package uk.gov.register.exceptions;

/**
 * Used if it is not possible to convert the data in a field to a particular datatype
 */
public class FieldConversionException extends RuntimeException {
    public FieldConversionException(final String message) {
        super(message);
    }
}
