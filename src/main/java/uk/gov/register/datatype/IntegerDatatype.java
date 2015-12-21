package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class IntegerDatatype implements Datatype {
    private final String datatypeName;

    public IntegerDatatype(String datatypeName) {
        this.datatypeName = datatypeName;
    }

    @Override
    public boolean isValid(JsonNode value) {
        return value.isTextual() && value.textValue().matches("^[-]?\\d+$");
    }

    public String getName() {
        return datatypeName;
    }
}
