package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Set;

public class EntryView {
    private final int serialNumber;
    private final String hash;
    private final String registerName;
    private final Map<String, FieldValue> entryMap;

    public EntryView(int serialNumber, String hash, String registerName, Map<String, FieldValue> entryMap) {
        this.hash = hash;
        this.registerName = registerName;
        this.entryMap = entryMap;
        this.serialNumber = serialNumber;
    }


    @JsonProperty("entry")
    public Map<String, FieldValue> getContent() {
        return entryMap;
    }

    @JsonProperty("hash")
    public String getHash() {
        return hash;
    }

    public FieldValue getField(String fieldName) {
        return entryMap.get(fieldName);
    }

    @SuppressWarnings("unused, used from html templates")
    public int getSerialNumber() {
        return serialNumber;
    }

    @SuppressWarnings("unused, used from html templates")
    public String primaryKey() {
        return entryMap.get(registerName).value();
    }

    @SuppressWarnings("unused, used from html templates")
    public String registerName() {
        return registerName;
    }

    public Set<String> allFields() {
        // TODO: this should return all possible fields, not just all present fields
        return entryMap.keySet();
    }
}


