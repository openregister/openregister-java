package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class CurieDatatype extends StringDatatype {

    @Override
    public boolean isValid(JsonNode value) {
        return super.isValid(value) &&
                value.textValue().contains(":");
    }
}
