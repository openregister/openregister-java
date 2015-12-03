package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class DummyDatatype implements Datatype{

    @Override
    public boolean isValid(JsonNode value) {
        return true;
    }

    @Override
    public boolean hasValue(JsonNode value) {
        return true;
    }
}
