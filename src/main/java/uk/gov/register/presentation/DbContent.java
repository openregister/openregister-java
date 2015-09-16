package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class DbContent {
    final String hash;
    final JsonNode jsonContent;

    @JsonCreator
    public DbContent(@JsonProperty("hash") String hash, @JsonProperty("entry") JsonNode jsonContent) {
        this.hash = hash;
        this.jsonContent = jsonContent;
    }

    public String getHash() {
        return hash;
    }

    public JsonNode getContent() {
        return jsonContent;
    }
}
