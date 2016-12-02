package uk.gov.register.core;

import org.glassfish.hk2.api.Factory;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.configuration.RegistersConfiguration;

import javax.inject.Inject;

public class RegisterDataFactory implements Factory<RegisterData> {
    private RegistersConfiguration registersConfiguration;
    private RegisterNameConfiguration configuration;

    @Inject
    public RegisterDataFactory(RegistersConfiguration registersConfiguration, RegisterNameConfiguration configuration) {
        this.registersConfiguration = registersConfiguration;
        this.configuration = configuration;
    }

    @Override
    public RegisterData provide() {
        return registersConfiguration.getRegisterData(configuration.getRegisterName());
    }

    @Override
    public void dispose(RegisterData instance) {

    }
}
