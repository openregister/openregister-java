package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class IntegerDatatype implements Datatype {

    @Override
    public boolean isValid(JsonNode value) {
        return value.isInt();
    }

    @Override
    public boolean valueExists(JsonNode value) {
        return value.asInt() != 0;
    }
}
