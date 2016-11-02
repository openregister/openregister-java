package uk.gov.register.configuration;

import javax.inject.Inject;

public class RegisterFieldsConfiguration {
    private final RegistersConfiguration registersConfiguration;
    private final String registerName;

    @Inject
    public RegisterFieldsConfiguration(RegistersConfiguration registersConfiguration, RegisterNameConfiguration registerNameConfiguration) {
        this.registersConfiguration = registersConfiguration;
        this.registerName = registerNameConfiguration.getRegister();
    }

    public Iterable<String> getFields() {
        return registersConfiguration.getRegisterData(registerName).getRegister().getFields();
    }
}