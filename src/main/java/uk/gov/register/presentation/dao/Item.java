package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Item{
    private final String sha256hex;
    public final JsonNode content;

    public Item(String sha256hex, JsonNode content) {
        this.sha256hex = sha256hex;
        this.content = content;
    }

    public Stream<Map.Entry<String, JsonNode>> getFieldsStream() {
        return StreamSupport.stream(((Iterable<Map.Entry<String, JsonNode>>) content::fields).spliterator(), false);
    }
}
