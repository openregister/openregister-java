package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

public class StringDatatype implements Datatype {

    @Override
    public boolean isValid(JsonNode value) {
        return value.isTextual();
    }

    @Override
    public boolean valueExists(JsonNode value) {
        return StringUtils.isNotBlank(value.textValue());
    }
}
