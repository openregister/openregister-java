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

    public Iterable<JsonNode> reconstruct(String[] jsonObjectsAsStrings) {
        return Iterables.transform(Arrays.asList(jsonObjectsAsStrings), e -> canonicalJsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8)));
    }

    public Iterable<JsonNode> reconstructWithoutCanonicalization(String[] jsonObjectsAsStrings) {
        return Iterables.transform(Arrays.asList(jsonObjectsAsStrings), e -> jsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8)));
    }

    public boolean verifyCanonicalization(String jsonObject) {
        byte[] objBytes = jsonObject.getBytes();
        return canonicalJsonMapper.readFromBytes(objBytes) == jsonMapper.readFromBytes(objBytes);
    }
}