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

    public String getName(){
        return datatypeName;
    }
}
