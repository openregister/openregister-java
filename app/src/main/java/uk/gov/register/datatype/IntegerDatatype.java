package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class IntegerDatatype implements Datatype {

    @Override
    public boolean isValid(JsonNode value) {
        if (value instanceof TextNode) {
            return value.textValue().matches("^[-]?\\d+$");
        } else {
            return value.isInt();
        }
    }

}
