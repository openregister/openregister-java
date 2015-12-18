package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;

public class EntryValidationException extends RuntimeException {
    private JsonNode entry;

    public EntryValidationException(String message, JsonNode entry) {
        super(message);
        this.entry = entry;
    }

    public JsonNode getEntry() {
        return entry;
    }
}
