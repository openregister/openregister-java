package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterables;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class EntryView {
    private final int serialNumber;
    private final String hash;
    private final String registerName;
    private final Map<String, FieldValue> entryMap;

    public EntryView(int serialNumber, String hash, String registerName, Map<String, FieldValue> entryMap) {
        this.hash = hash;
        this.registerName = registerName;
        this.entryMap = new TreeMap<>(entryMap); // TreeMap so we get fields in sorted order
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
    public Iterable<Map.Entry<String, FieldValue>> getNonPrimaryFields() {
        return Iterables.filter(entryMap.entrySet(), e -> !e.getKey().equals(registerName));
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


