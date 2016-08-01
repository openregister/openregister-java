package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class IntegerDatatype implements Datatype {
    private final String datatypeName;

    public IntegerDatatype(String datatypeName) {
        this.datatypeName = datatypeName;
    }

    @Override
    public boolean isValid(JsonNode value) {
        return value.isTextual() && value.textValue().matches("^-?[1-9][0-9]*|0$");
    }

    public String getName() {
        return datatypeName;
    }
}
