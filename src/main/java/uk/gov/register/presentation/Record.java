package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Record {
    private final String hash;
    private final JsonNode content;

    @JsonCreator
    public Record(@JsonProperty("hash") String hash, @JsonProperty("entry") JsonNode content) {
        this.hash = hash;
        this.content = content;
    }

    @JsonProperty
    public String getHash() {
        return hash;
    }

    @JsonProperty("entry")
    public JsonNode getContent() {
        return content;
    }
}
