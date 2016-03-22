package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.JsonNode;

public class Item{
    private final String sha256hex;
    public final JsonNode content;

    public Item(String sha256hex, JsonNode content) {
        this.sha256hex = sha256hex;
        this.content = content;
    }
}
