package uk.gov.register.service;

import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Field;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.exceptions.FieldDefinitionException;
import uk.gov.register.exceptions.RegisterDefinitionException;
import uk.gov.register.util.FieldComparer;
import uk.gov.register.util.RegisterComparer;

public class EnvironmentValidator {

    private final ConfigManager configManager;

    public EnvironmentValidator(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void validateRegisterAgainstEnvironment(RegisterMetadata registerMetadata) {
        RegisterMetadata environmentRegisterMetadata = configManager.getRegistersConfiguration().getRegisterMetadata(registerMetadata.getRegisterId());

        if (!RegisterComparer.equals(registerMetadata, environmentRegisterMetadata)) {
            throw new RegisterDefinitionException(registerMetadata.getRegisterId());
        }
    }

    public void validateFieldAgainstEnvironment(Field localField) throws FieldDefinitionException {
        Field environmentField = configManager.getFieldsConfiguration().getField(localField.fieldName)
                .orElseThrow(() -> new FieldDefinitionException("Field " + localField.fieldName + " does not exist in Field Register"));

        if (!FieldComparer.equals(localField, environmentField)) {
            throw new FieldDefinitionException("Definition of field " + localField.fieldName + " does not match Field Register");
        }
    }

    public void validateExistingMetadataAgainstEnvironment(RegisterContext registerContext)
            throws FieldDefinitionException, RegisterDefinitionException {
        Register register = registerContext.buildOnDemandRegister();

        if (register.getTotalRecords(EntryType.system) == 0) {
            return;
        }

        RegisterMetadata localRegisterMetadata = register.getRegisterMetadata();
        register.getFieldsByName().values().forEach(this::validateFieldAgainstEnvironment);
        validateRegisterAgainstEnvironment(localRegisterMetadata);
    }
}
