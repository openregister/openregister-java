package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterators;
import jersey.repackaged.com.google.common.collect.Sets;
import uk.gov.register.*;
import uk.gov.register.datatype.Datatype;

import java.util.Set;

public class EntryValidator {

    private final FieldsConfiguration fieldsConfiguration;
    private final RegistersConfiguration registersConfiguration;

    public EntryValidator(RegistersConfiguration registersConfiguration, FieldsConfiguration fieldsConfiguration) {
        this.fieldsConfiguration = fieldsConfiguration;
        this.registersConfiguration = registersConfiguration;
    }

    public void validateEntry(String registerName, JsonNode jsonNode) throws EntryValidationException {
        Register register = registersConfiguration.getRegister(registerName);

        validateFields(jsonNode, register);

        validatePrimaryKeyExists(jsonNode, register.getRegisterName());

        validateFieldsValue(jsonNode);
    }

    private void validatePrimaryKeyExists(JsonNode jsonNode, String registerName) throws EntryValidationException {
        throwEntryValidationExceptionIfConditionIsFalse(jsonNode.get(registerName) == null, jsonNode, "Entry does not contain primary key field '" + registerName + "'");
    }

    private void validateFields(JsonNode jsonNode, Register register) throws EntryValidationException {
        Set<String> unknownFields = Sets.newHashSet(
                Iterators.filter(jsonNode.fieldNames(), fieldName -> !register.containsField(fieldName))
        );

        throwEntryValidationExceptionIfConditionIsFalse(!unknownFields.isEmpty(), jsonNode, "Entry contains invalid fields: " + unknownFields.toString());
    }

    private void validateFieldsValue(JsonNode jsonNode) throws EntryValidationException {
        jsonNode.fieldNames().forEachRemaining(fieldName -> {
            Field field = fieldsConfiguration.getField(fieldName);

            Datatype datatype = field.getDatatype();

            JsonNode fieldValue = jsonNode.get(fieldName);

            if (field.getCardinality().equals(Cardinality.MANY)) {

                throwEntryValidationExceptionIfConditionIsFalse(!fieldValue.isArray(), jsonNode, String.format("Field '%s' has cardinality 'n' so the value must be an array of '%s'", fieldName, datatype.getName()));

                fieldValue.elements().forEachRemaining(element -> {
                    throwEntryValidationExceptionIfConditionIsFalse(!datatype.isValid(element), jsonNode, String.format("Field '%s' values must be of type '%s'", fieldName, datatype.getName()));
                });

            } else {
                throwEntryValidationExceptionIfConditionIsFalse(!datatype.isValid(fieldValue), jsonNode, String.format("Field '%s' value must be of type '%s'", fieldName, datatype.getName()));
            }

        });
    }

    private void throwEntryValidationExceptionIfConditionIsFalse(boolean condition, JsonNode inputJsonEntry, String errorMessage) {
        if (condition) {
            throw new EntryValidationException(errorMessage, inputJsonEntry);
        }
    }
}
