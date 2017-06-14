package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import org.jvnet.hk2.annotations.Service;
import uk.gov.register.core.*;

import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static uk.gov.register.core.Cardinality.ONE;

@Service
public class ItemConverter {

    public Map<String, FieldValue> convertItem(Item item, final Map<String, Field> fieldsByName) {
        return item.getFieldsStream().collect(toMap(Map.Entry::getKey, e -> convert(e, fieldsByName)));
    }

    FieldValue convert(Map.Entry<String, JsonNode> fieldNameToJson, Map<String, Field> fieldsByName) {
        String fieldName = fieldNameToJson.getKey();
        JsonNode value = fieldNameToJson.getValue();
        return convert(value, fieldsByName.get(fieldName));
    }

    private FieldValue convert(JsonNode propertyJson, final Field field) {
        Cardinality cardinality = field.getCardinality();
        if (cardinality == ONE) {
            return convertScalar(propertyJson, field);
        } else {
            return new ListValue(Iterables.transform(propertyJson, listElementJson -> convertScalar(listElementJson, field)));
        }
    }

    private FieldValue convertScalar(JsonNode value, Field field) {
        if (field.getDatatype().getName().equals("curie")) {
            if (value.textValue().contains(":")) {
                return new LinkValue.CurieValue(value.textValue());
            }
            return new LinkValue(field.getRegister().get(), value.textValue());
        } else if (field.getRegister().isPresent()) {
            return new LinkValue(field.getRegister().get(), value.textValue());
            //Note: the equals check below must be replaced with the specified datatype, instead of doing string comparision
            // We should replace this once the datatype register is available
        } else {
            return new StringValue(value.textValue());
        }
    }

}
