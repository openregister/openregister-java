package uk.gov.register.exceptions;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Used when a blob fails validation against a register's schema, for example
 */
public class BlobValidationException extends RuntimeException {
    private JsonNode entry;

    public BlobValidationException(String message, JsonNode entry) {
        super(message);
        this.entry = entry;
    }

    public JsonNode getEntry() {
        return entry;
    }
}
