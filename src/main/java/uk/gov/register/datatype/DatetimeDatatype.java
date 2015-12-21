package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;

public class DatetimeDatatype implements Datatype {
    private final String datatypeName;

    public DatetimeDatatype(String datatypeName) {
        this.datatypeName = datatypeName;
    }

    //TODO: implement the validations for Datetime datatype
    @Override
    public boolean isValid(JsonNode value) {
        return true;
    }

    public String getName() {
        return datatypeName;
    }
}
