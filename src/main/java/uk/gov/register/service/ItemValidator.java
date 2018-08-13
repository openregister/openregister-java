package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.datatype.CurieDatatype;
import uk.gov.register.core.datatype.Datatype;
import uk.gov.register.exceptions.ItemValidationException;

import java.util.Map;
import java.util.Set;

public class ItemValidator {
    private final RegisterId registerId;

    public ItemValidator(RegisterId registerId) {
        this.registerId = registerId;
    }

    public void validateItem(JsonNode inputEntry, Map<String, Field> fields, RegisterMetadata registerMetadata) throws ItemValidationException {
        validateFields(inputEntry, registerMetadata);

        validatePrimaryKeyExists(inputEntry);

        validateFieldsValue(inputEntry, fields);
    }

    private void validatePrimaryKeyExists(JsonNode inputEntry) throws ItemValidationException {
        JsonNode primaryKeyNode = inputEntry.get(registerId.value());
        throwItemValidationExceptionIfConditionIsFalse(primaryKeyNode == null, inputEntry, "Entry does not contain primary key field '" + registerId + "'");
        validatePrimaryKeyIsNotBlankAssumingItWillAlwaysBeAStringNode(StringUtils.isBlank(primaryKeyNode.textValue()), inputEntry, "Primary key field '" + registerId + "' must have a valid value");
    }

    private void validateFields(JsonNode inputEntry, RegisterMetadata registerMetadata) throws ItemValidationException {
        Set<String> inputFieldNames = Sets.newHashSet(inputEntry.fieldNames());
        Set<String> expectedFieldNames = Sets.newHashSet(registerMetadata.getFields());
        Set<String> unknownFields = Sets.difference(inputFieldNames, expectedFieldNames);

        throwItemValidationExceptionIfConditionIsFalse(!unknownFields.isEmpty(), inputEntry, "Entry contains invalid fields: " + unknownFields.toString());
    }

    private void validateFieldsValue(JsonNode inputEntry, Map<String, Field> fields) throws ItemValidationException {
        inputEntry.fieldNames().forEachRemaining(fieldName -> {
            Field field = fields.get(fieldName);
            JsonNode fieldValue = inputEntry.get(fieldName);

            if (field.getCardinality().equals(Cardinality.MANY)) {
                throwItemValidationExceptionIfConditionIsFalse(!fieldValue.isArray(), inputEntry, String.format("Field '%s' has cardinality 'n' so the value must be an array of '%s'", fieldName, field.getDatatype().getName()));

                fieldValue.elements().forEachRemaining(element -> validateSingleValue(field, element, inputEntry));
            } else {
                validateSingleValue(field, fieldValue, inputEntry);
            }
        });
    }

    private void validatePrimaryKeyIsNotBlankAssumingItWillAlwaysBeAStringNode(boolean condition, JsonNode inputJsonEntry, String errorMessage) {
        throwItemValidationExceptionIfConditionIsFalse(condition, inputJsonEntry, errorMessage);
    }

    private void validateSingleValue(Field field, JsonNode value, JsonNode inputEntry) {
        Datatype datatype = field.getDatatype();
        String fieldName = field.fieldName;

        throwItemValidationExceptionIfConditionIsFalse(!datatype.isValid(value), inputEntry,
                String.format("Field '%s' %s must be of type '%s'", fieldName, field.getCardinality().equals(Cardinality.MANY) ? "values" : "value", datatype.getName()));

        if ("curie".equals(datatype.getName()) && !value.textValue().contains(CurieDatatype.CURIE_SEPARATOR)) {
            throwItemValidationExceptionIfConditionIsFalse(
                    !field.getRegister().isPresent(), inputEntry,
                    String.format("Field '%s' must contain a curie in a valid format or the '%s' field specified.", fieldName, "register"));
        }
    }

    private void throwItemValidationExceptionIfConditionIsFalse(boolean condition, JsonNode inputJsonEntry, String errorMessage) {
        if (condition) {
            throw new ItemValidationException(errorMessage, inputJsonEntry);
        }
    }
}
