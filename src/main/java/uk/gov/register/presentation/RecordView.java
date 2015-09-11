package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Set;

public class RecordView {
    private final String registerName;
    private final Map<String, FieldValue> entryMap;
    private String hash;

    public RecordView(String hash, String registerName, Map<String, FieldValue> entryMap) {
        this.hash = hash;
        this.registerName = registerName;
        this.entryMap = entryMap;
    }


    @JsonProperty("entry")
    public Map<String, FieldValue> getEntry() {
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


