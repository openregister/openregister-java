package uk.gov.register.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ObjectReconstructor {
    private JsonMapper jsonMapper;
    private CanonicalJsonMapper canonicalJsonMapper;

    public ObjectReconstructor() {
        this.jsonMapper = new JsonMapper();
        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    public JsonNode reconstruct(String jsonObjectAsString) {
        return canonicalJsonMapper.readFromBytes(jsonObjectAsString.getBytes(StandardCharsets.UTF_8));
    }

    public Iterable<JsonNode> reconstruct(String[] jsonObjectsAsStrings) {
        return Iterables.transform(Arrays.asList(jsonObjectsAsStrings), e -> canonicalJsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8)));
    }

    public JsonNode reconstructWithoutCanonicalization(String jsonObjectAsString) {
        return jsonMapper.readFromBytes(jsonObjectAsString.getBytes(StandardCharsets.UTF_8));
    }
}