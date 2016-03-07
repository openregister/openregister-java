package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectReconstructor {
    private CanonicalJsonMapper canonicalJsonMapper;

    public ObjectReconstructor() {
        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    public List<JsonNode> reconstruct(String[] jsonObjectsAsStrings) {
        return Arrays.stream(jsonObjectsAsStrings)
                .map(e -> {
                    try {
                        return canonicalJsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8));
                    } catch (Exception ex) {
                        return ExceptionUtils.rethrow(ex);
                    }
                })
                .collect(Collectors.toList());
    }
}
