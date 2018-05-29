package uk.gov.register.exceptions;

/**
 * Used if an item is not canonical JSON format
 */
public class ItemNotCanonicalException extends RuntimeException {
    public ItemNotCanonicalException(String jsonString) {
        super(jsonString);
    }
}
