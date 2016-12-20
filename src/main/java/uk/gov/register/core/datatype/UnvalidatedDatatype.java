package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class UnvalidatedDatatype implements Datatype{

    private final String datatypeName;

    public UnvalidatedDatatype(String datatypeName) {
        this.datatypeName = datatypeName;
    }

    @Override
    public boolean isValid(JsonNode value) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnvalidatedDatatype that = (UnvalidatedDatatype) o;

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
