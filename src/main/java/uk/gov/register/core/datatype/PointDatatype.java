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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PointDatatype that = (PointDatatype) o;

        return datatypeName != null ? datatypeName.equals(that.datatypeName) : that.datatypeName == null;

    }

    @Override
    public int hashCode() {
        return datatypeName != null ? datatypeName.hashCode() : 0;
    }

    public String getName(){
        return datatypeName;
    }
}
