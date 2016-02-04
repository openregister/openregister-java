package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

public class StringDatatype implements Datatype {
    private final String datatypeName;

    public StringDatatype(String datatypeName) {
        this.datatypeName = datatypeName;
    }

    @Override
    public boolean isValid(JsonNode value) {
        if (value.isTextual() && StringUtils.isNotBlank(value.textValue())) {
            return true;
        }
        return false;
    }

    public String getName() {
        return datatypeName;
    }
}
