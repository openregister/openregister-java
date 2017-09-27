package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class IntegerDatatype extends AbstractDatatype {

    public IntegerDatatype(String datatypeName) {
        super(datatypeName);
    }

    @Override
    public boolean isValid(JsonNode value) {
        return value.isTextual() && value.textValue().matches("^-?[1-9][0-9]*|0$");
    }
}
