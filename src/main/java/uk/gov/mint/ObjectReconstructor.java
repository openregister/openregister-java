package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import uk.gov.register.util.CanonicalJsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ObjectReconstructor {
    private CanonicalJsonMapper canonicalJsonMapper;

    public ObjectReconstructor() {
        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    public Iterable<JsonNode> reconstruct(String[] jsonObjectsAsStrings) {
        return Iterables.transform(Arrays.asList(jsonObjectsAsStrings), e -> canonicalJsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8)));
    }
}
