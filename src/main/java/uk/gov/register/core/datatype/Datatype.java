package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public interface Datatype {
    boolean isValid(JsonNode value);

    String getName();
}

