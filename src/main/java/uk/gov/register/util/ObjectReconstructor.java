package uk.gov.register.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ObjectReconstructor {
    private CanonicalJsonMapper canonicalJsonMapper;

    public ObjectReconstructor() {
        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    public JsonNode reconstruct(String jsonObjectsAsString) throws IOException {
        return new ObjectMapper().readValue(jsonObjectsAsString.getBytes(StandardCharsets.UTF_8), JsonNode.class);
    }

    public Iterable<JsonNode> reconstructWithCanonicalization(String[] jsonObjectsAsStrings) {
        return Iterables.transform(Arrays.asList(jsonObjectsAsStrings), e -> canonicalJsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8)));
    }
}