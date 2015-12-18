package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class StringDatatype implements Datatype {

    @Override
    public boolean isValid(JsonNode value) {
        return value.isTextual();
    }

}
