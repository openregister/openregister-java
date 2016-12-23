package uk.gov.register.configuration;

import org.junit.Test;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterName;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

public class RegistersConfigurationFunctionalTest {
    @Test
    public void configuration_shouldReturnRegisterData_whenRegisterExists() {
        RegistersConfiguration configuration = new RegistersConfiguration(Optional.ofNullable(System.getProperty("registersYaml")));
        RegisterMetadata data = configuration.getRegisterMetadata(new RegisterName("register"));

        assertThat("register", equalTo(data.getRegisterName().toString()));
    }

    @Test
    public void configuration_shouldThrowException_whenRegisterDoesNotExist() {
        try {
            RegistersConfiguration configuration = new RegistersConfiguration(Optional.ofNullable(System.getProperty("registersYaml")));
            configuration.getRegisterMetadata(new RegisterName("register-that-does-not-exist"));
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), equalTo("Cannot get register data for register-that-does-not-exist"));
        }
    }
}