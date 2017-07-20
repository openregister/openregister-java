package uk.gov.register.service;

import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.exceptions.FieldValidationException;
import uk.gov.register.exceptions.RegisterValidationException;
import uk.gov.register.util.FieldComparer;

import java.util.List;

public class EnvironmentValidator {

    private final ConfigManager configManager;

    public EnvironmentValidator(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void validateRegisterAgainstEnvironment(RegisterMetadata registerMetadata) {
        RegisterMetadata environmentRegisterMetadata = configManager.getRegistersConfiguration().getRegisterMetadata(registerMetadata.getRegisterName());
        List<String> environmentRegisterFields = environmentRegisterMetadata.getFields();
        List<String> localRegisterFields = registerMetadata.getFields();

        if (environmentRegisterFields.size() != localRegisterFields.size() || !environmentRegisterFields.containsAll(localRegisterFields)) {
            throw new RegisterValidationException(registerMetadata.getRegisterName());
        }
    }
    
    public void validateFieldAgainstEnvironment(Field localField) {
        Field environmentField = configManager.getFieldsConfiguration().getField(localField.fieldName)
                .orElseThrow(() -> new FieldValidationException("Field " + localField.fieldName + " does not exist in Field Register"));

        if (!FieldComparer.equals(localField, environmentField)) {
            throw new FieldValidationException("Definition of field " + localField.fieldName + " does not match Field Register");
        }
    }
}
