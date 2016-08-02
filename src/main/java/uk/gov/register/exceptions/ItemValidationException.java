package uk.gov.register.exceptions;

import com.fasterxml.jackson.databind.JsonNode;

public class ItemValidationException extends RuntimeException {
    private JsonNode entry;

    public ItemValidationException(String message, JsonNode entry) {
        super(message);
        this.entry = entry;
    }

    public JsonNode getEntry() {
        return entry;
    }
}
