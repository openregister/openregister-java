package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class PointDatatype extends AbstractDatatype {

    public PointDatatype(String datatypeName) {
        super(datatypeName);
    }

    @Override
    public boolean isValid(JsonNode value) {
        return value.textValue().trim().matches("^\\[\\s*[-]?\\d+\\.\\d+\\s*,\\s*[-]?\\d+\\.\\d+\\s*\\]$");
    }
}
