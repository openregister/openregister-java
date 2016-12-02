package uk.gov.register.core;

import org.glassfish.hk2.api.Factory;
import uk.gov.register.configuration.RegisterFieldsConfiguration;

import javax.inject.Inject;

import static com.google.common.collect.Lists.newArrayList;

public class RegisterFieldsConfigurationFactory implements Factory<RegisterFieldsConfiguration> {
    private final RegisterData registerData;

    @Inject
    public RegisterFieldsConfigurationFactory(RegisterData registerData) {
        this.registerData = registerData;
    }

    @Override
    public RegisterFieldsConfiguration provide() {
        return new RegisterFieldsConfiguration(newArrayList(registerData.getRegister().getFields()));
    }

    @Override
    public void dispose(RegisterFieldsConfiguration instance) {

    }
}
