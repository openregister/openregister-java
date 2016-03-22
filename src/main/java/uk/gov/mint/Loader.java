package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;

public interface Loader {
    void load(Iterable<JsonNode> entries);
}
