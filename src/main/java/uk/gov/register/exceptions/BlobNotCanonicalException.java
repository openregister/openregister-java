package uk.gov.register.exceptions;

/**
 * Used if an item is not canonical JSON format
 */
public class BlobNotCanonicalException extends RuntimeException {
    public BlobNotCanonicalException(String jsonString) {
        super(jsonString);
    }
}
