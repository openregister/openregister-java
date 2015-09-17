package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EntryView {
    private final int serialNumber;
    private final String hash;
    private final String registerName;
    private final Map<String, FieldValue> entryMap;
    private final Map<String, FieldValue> nonPrimaryFields;

    public EntryView(int serialNumber, String hash, String registerName, Map<String, FieldValue> entryMap) {
        this.hash = hash;
        this.registerName = registerName;
        this.entryMap = entryMap;
        this.nonPrimaryFields = entryMap.entrySet().stream()
                .filter(entry -> !Objects.equals(entry.getKey(), registerName))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

    @JsonProperty("serial-number")
    public int getSerialNumber() {
        return serialNumber;
    }

    @SuppressWarnings("unused, used from html templates")
    @JsonIgnore
    public Map<String, FieldValue> getNonPrimaryFields() {
        return nonPrimaryFields;
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


