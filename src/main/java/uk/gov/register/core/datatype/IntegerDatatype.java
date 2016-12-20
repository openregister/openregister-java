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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntegerDatatype that = (IntegerDatatype) o;

        return datatypeName != null ? datatypeName.equals(that.datatypeName) : that.datatypeName == null;

    }

    @Override
    public int hashCode() {
        return datatypeName != null ? datatypeName.hashCode() : 0;
    }

    public String getName() {
        return datatypeName;
    }
}
