package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.StringUtils;

public class IntegerDatatype implements Datatype {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public boolean isValid(JsonNode value) {
        if (value instanceof TextNode) {
            return value.textValue().matches("^[-]?\\d+$");
        } else {
            return value.isInt();
        }
    }

    @Override
    public boolean hasValue(JsonNode value) {
        //note: value should not be a text node in reality but in loader we are creating json from csv/tsv data and a string value is generated always.
        return !(value instanceof TextNode) || StringUtils.isNotBlank(value.textValue());
    }
}
