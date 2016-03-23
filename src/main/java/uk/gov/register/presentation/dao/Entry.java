package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Entry {
    @JsonProperty("entry-number")
    public final String entryNumber;

    private final String sha256hex;

    @JsonProperty("entry-timestamp")
    public final String timestamp;

    public Entry(String entryNumber, String sha256hex, String timestamp) {
        this.entryNumber = entryNumber;
        this.sha256hex = sha256hex;
        this.timestamp = timestamp;
    }

    @JsonProperty("item-hash")
    public String getSha256hex() {
        return "sha-256:" + sha256hex;
    }
}
