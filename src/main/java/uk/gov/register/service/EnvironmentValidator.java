package uk.gov.register.service;

import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.core.Field;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.exceptions.FieldValidationException;
import uk.gov.register.exceptions.RegisterValidationException;
import uk.gov.register.util.FieldComparer;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;
import uk.gov.register.util.RegisterComparer;

import java.util.List;

public class EnvironmentValidator {

    private final ConfigManager configManager;

    public EnvironmentValidator(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void validateRegisterAgainstEnvironment(RegisterMetadata registerMetadata) {
        RegisterMetadata environmentRegisterMetadata = configManager.getRegistersConfiguration().getRegisterMetadata(registerMetadata.getRegisterName());

        if (!RegisterComparer.equals(registerMetadata, environmentRegisterMetadata)) {
            throw new RegisterValidationException(registerMetadata.getRegisterName());
        }
    }
    
    public void validateFieldAgainstEnvironment(Field localField) {
        System.out.println(localField.fieldName);
        Field environmentField = configManager.getFieldsConfiguration().getField(localField.fieldName)
                .orElseThrow(() -> new FieldValidationException("Field " + localField.fieldName + " does not exist in Field Register"));

        if (!FieldComparer.equals(localField, environmentField)) {
            throw new FieldValidationException("Definition of field " + localField.fieldName + " does not match Field Register");
        }
    }
    
    public void validateExistingMetadataAgainstEnvironment(RegisterContext registerContext) {
        Register register = registerContext.buildOnDemandRegister();

        if (register.getTotalDerivationRecords(IndexNames.METADATA) == 0) {
            return;
        }
        
        RegisterMetadata localRegisterMetadata = register.getRegisterMetadata();
        register.getFieldsByName().values().forEach(f -> validateFieldAgainstEnvironment(f));
        validateRegisterAgainstEnvironment(localRegisterMetadata);
    }
}