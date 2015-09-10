package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @SuppressWarnings("unused, used from html templates")
    @JsonIgnore
    public String getPrimaryKey() {
        return entryMap.get(registerName).value();
    }

    @JsonProperty("entry")
    public Map<String, FieldValue> getEntry() {
        return entryMap;
    }

    public FieldValue getField(String fieldName) {
        return entryMap.get(fieldName);
    }

    @JsonProperty("hash")
    public String getHash() {
        return hash;
    }

    public Set<String> allFields() {
        // TODO: this should return all possible fields, not just all present fields
        return entryMap.keySet();
    }
}


