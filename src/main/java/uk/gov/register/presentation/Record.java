package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.presentation.mapper.JsonObjectMapper;

import java.util.Map;

public class Record {
    private final String hash;
    private final JsonNode jsonEntry;

    @JsonCreator
    public Record(@JsonProperty("hash") String hash, @JsonProperty("entry") JsonNode jsonEntry) {
        this.hash = hash;
        this.jsonEntry = jsonEntry;
    }

    @JsonProperty
    public String getHash() {
        return hash;
    }

    @JsonProperty("entry")
    public Map<String, Object> getEntry() {
        return JsonObjectMapper.convert(jsonEntry, new TypeReference<Map<String, Object>>() {
        });
    }
}
