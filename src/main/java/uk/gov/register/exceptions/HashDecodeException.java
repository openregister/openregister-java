package uk.gov.register.exceptions;

/**
 * Used if a string value cannot be decoded into a HashValue
 */
public class HashDecodeException extends RuntimeException {
    public HashDecodeException(String message) {
        super(message);
    }
}
