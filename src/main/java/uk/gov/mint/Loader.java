package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface Loader {
    void load(List<JsonNode> entries);
}
