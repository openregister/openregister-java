package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class UnvalidatedDatatype extends AbstractDatatype {

    public UnvalidatedDatatype(String datatypeName) {
        super(datatypeName);
    }

    @Override
    public boolean isValid(JsonNode value) {
        return true;
    }
}
