package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import uk.gov.register.*;
import uk.gov.register.datatype.Datatype;
import uk.gov.register.datatype.DatatypeFactory;

import java.util.Iterator;

public class EntryValidator {

    private final FieldsConfiguration fieldsConfiguration;
    private final RegistersConfiguration registersConfiguration;

    public EntryValidator(RegistersConfiguration registersConfiguration, FieldsConfiguration fieldsConfiguration) {
        this.fieldsConfiguration = fieldsConfiguration;
        this.registersConfiguration = registersConfiguration;
    }

    public void validateEntry(String registerName, JsonNode jsonNode) throws EntryValidationException{
        Register register = registersConfiguration.getRegister(registerName);
        validateFields(register, jsonNode);
        validatePrimaryKeyExists(register, jsonNode);
        validateFieldsValue(jsonNode);
    }

    private void validatePrimaryKeyExists(Register register, JsonNode jsonNode) throws EntryValidationException {
        String primaryKey = register.getRegisterName();

        JsonNode valueNode = jsonNode.get(primaryKey);

        if (valueNode == null) {
            throw new EntryValidationException("Register's primary key field not available.", jsonNode);
        }

        Field field = fieldsConfiguration.getField(primaryKey);

        Datatype datatype = DatatypeFactory.get(field.getDatatype());

        if (!datatype.valueExists(valueNode)) {
            throw new EntryValidationException("Value for primary key field not exists.", jsonNode);
        }

    }

    private void validateFieldsValue(JsonNode jsonNode) throws EntryValidationException {
        FieldsConfiguration fieldsConfiguration = new FieldsConfiguration();
        Iterator<String> fieldIterator = jsonNode.fieldNames();
        while (fieldIterator.hasNext()) {
            String fieldName = fieldIterator.next();
            Field field = fieldsConfiguration.getField(fieldName);

            Datatype datatype = DatatypeFactory.get(field.getDatatype());
            if (!datatype.isValid(jsonNode.get(fieldName))) {
                throw new EntryValidationException("Check field '" + fieldName + "' value, must be of acceptable datatype.", jsonNode);
            }
        }
    }

    private void validateFields(Register register, JsonNode jsonNode) throws EntryValidationException {
        Iterator<String> fieldNames = jsonNode.fieldNames();
        Iterable<String> registerFields = register.getFields();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (!Iterables.contains(registerFields, fieldName)) {
                throw new EntryValidationException("Unknown field '" + fieldName + "'.", jsonNode);
            }
        }
    }

    public static class EntryValidationException extends Exception {
        private JsonNode entry;

        public EntryValidationException(String message, JsonNode entry) {
            super(message);
            this.entry = entry;
        }

        public JsonNode getEntry() {
            return entry;
        }
    }
}
