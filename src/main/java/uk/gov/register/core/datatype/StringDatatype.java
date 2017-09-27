package uk.gov.register.core.datatype;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

public class StringDatatype extends AbstractDatatype {

    public StringDatatype(String datatypeName) {
        super(datatypeName);
    }

    @Override
    public boolean isValid(JsonNode value) {
        return value.isTextual() && StringUtils.isNotBlank(value.textValue());
    }
}
