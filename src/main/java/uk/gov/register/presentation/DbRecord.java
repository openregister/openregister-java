package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class DbRecord {
    private final String hash;
    private final JsonNode jsonEntry;

    @JsonCreator
    public DbRecord(@JsonProperty("hash") String hash, @JsonProperty("entry") JsonNode jsonEntry) {
        this.hash = hash;
        this.jsonEntry = jsonEntry;
    }

    public String getHash() {
        return hash;
    }

    public JsonNode getEntry() {
        return jsonEntry;
    }
}

