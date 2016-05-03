package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.*;
import uk.gov.register.datatype.Datatype;

import java.util.Set;

public class ItemValidator {

    private final FieldsConfiguration fieldsConfiguration;
    private final RegistersConfiguration registersConfiguration;

    public ItemValidator(RegistersConfiguration registersConfiguration, FieldsConfiguration fieldsConfiguration) {
        this.fieldsConfiguration = fieldsConfiguration;
        this.registersConfiguration = registersConfiguration;
    }

    public void validateItem(String registerName, JsonNode inputEntry) throws ItemValidationException {
        Register register = registersConfiguration.getRegister(registerName);

        validateFields(inputEntry, register);

        validatePrimaryKeyExists(inputEntry, register.getRegisterName());

        validateFieldsValue(inputEntry);
    }

    private void validatePrimaryKeyExists(JsonNode inputEntry, String registerName) throws ItemValidationException {
        JsonNode primaryKeyNode = inputEntry.get(registerName);
        throwEntryValidationExceptionIfConditionIsFalse(primaryKeyNode == null, inputEntry, "Entry does not contain primary key field '" + registerName + "'");
        validatePrimaryKeyIsNotBlankAssumingItWillAlwaysBeAStringNode(StringUtils.isBlank(primaryKeyNode.textValue()), inputEntry, "Primary key field '" + registerName + "' must have a valid value");
    }

    private void validateFields(JsonNode inputEntry, Register register) throws ItemValidationException {
        Set<String> unknownFields = Sets.newHashSet(
                Iterators.filter(inputEntry.fieldNames(), fieldName -> !register.containsField(fieldName))
        );

        throwEntryValidationExceptionIfConditionIsFalse(!unknownFields.isEmpty(), inputEntry, "Entry contains invalid fields: " + unknownFields.toString());
    }

    private void validateFieldsValue(JsonNode inputEntry) throws ItemValidationException {
        inputEntry.fieldNames().forEachRemaining(fieldName -> {
            Field field = fieldsConfiguration.getField(fieldName);

            Datatype datatype = field.getDatatype();

            JsonNode fieldValue = inputEntry.get(fieldName);

            if (field.getCardinality().equals(Cardinality.MANY)) {

                throwEntryValidationExceptionIfConditionIsFalse(!fieldValue.isArray(), inputEntry, String.format("Field '%s' has cardinality 'n' so the value must be an array of '%s'", fieldName, datatype.getName()));

                fieldValue.elements().forEachRemaining(element -> throwEntryValidationExceptionIfConditionIsFalse(!datatype.isValid(element), inputEntry, String.format("Field '%s' values must be of type '%s'", fieldName, datatype.getName())));

            } else {
                throwEntryValidationExceptionIfConditionIsFalse(!datatype.isValid(fieldValue), inputEntry, String.format("Field '%s' value must be of type '%s'", fieldName, datatype.getName()));
            }

        });
    }

    private void validatePrimaryKeyIsNotBlankAssumingItWillAlwaysBeAStringNode(boolean condition, JsonNode inputJsonEntry, String errorMessage) {
        throwEntryValidationExceptionIfConditionIsFalse(condition, inputJsonEntry, errorMessage);
    }

    private void throwEntryValidationExceptionIfConditionIsFalse(boolean condition, JsonNode inputJsonEntry, String errorMessage) {
        if (condition) {
            throw new ItemValidationException(errorMessage, inputJsonEntry);
        }
    }
}
