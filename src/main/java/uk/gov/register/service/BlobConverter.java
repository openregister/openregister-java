package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import org.jvnet.hk2.annotations.Service;
import uk.gov.register.core.*;
import uk.gov.register.exceptions.FieldConversionException;

import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static uk.gov.register.core.Cardinality.ONE;

@Service
public class BlobConverter {

    public Map<String, FieldValue> convertBlob(final Blob blob, final Map<String, Field> fieldsByName) throws FieldConversionException {
        return blob.getFieldsStream().collect(toMap(Map.Entry::getKey, e -> convert(e, fieldsByName)));
    }

    FieldValue convert(final Map.Entry<String, JsonNode> fieldNameToJson, final Map<String, Field> fieldsByName) throws FieldConversionException {
        final String fieldName = fieldNameToJson.getKey();
        final JsonNode value = fieldNameToJson.getValue();
        return convert(value, fieldsByName.get(fieldName));
    }

    private FieldValue convert(final JsonNode propertyJson, final Field field) throws FieldConversionException {
        final Cardinality cardinality = field.getCardinality();
        if (cardinality == ONE) {
            return convertScalar(propertyJson, field);
        } else {
            return new ListValue(Iterables.transform(propertyJson, listElementJson -> convertScalar(listElementJson, field)));
        }
    }

    private FieldValue convertScalar(final JsonNode value, final Field field) throws FieldConversionException {
        try {
            if (field.getDatatype().getName().equals("curie")) {
                if (value.textValue().contains(":")) {
                    return new RegisterLinkValue.CurieValue(value.textValue());
                }

                return new RegisterLinkValue(field.getRegister().get(), value.textValue());
            }
            else if (field.getDatatype().getName().equals("url")) {
                return new UrlValue(value.textValue());
            }
            else if (field.getRegister().isPresent()) {
                return new RegisterLinkValue(field.getRegister().get(), value.textValue());
                //Note: the equals check below must be replaced with the specified datatype, instead of doing string comparision
                // We should replace this once the datatype register is available
            } else {
                return new StringValue(value.textValue());
            }
        } catch (final Exception exception) {
            throw new FieldConversionException(
                    String.format("Not possible to process the field (name, data-type, value): %s, %s, %s.",
                            field.fieldName, field.getDatatype().getName(), value.textValue()));
        }
    }

}
