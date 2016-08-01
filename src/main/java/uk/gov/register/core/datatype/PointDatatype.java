package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class PointDatatype implements Datatype {
    private final String datatypeName;

    public PointDatatype(String datatypeName) {
        this.datatypeName = datatypeName;
    }

    @Override
    public boolean isValid(JsonNode value) {
        return value.textValue().trim().matches("^\\[\\s*[-]?\\d+\\.\\d+\\s*,\\s*[-]?\\d+\\.\\d+\\s*\\]$");
    }

    public String getName(){
        return datatypeName;
    }
}
