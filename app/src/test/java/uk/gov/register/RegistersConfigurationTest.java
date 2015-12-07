package uk.gov.register;

import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class RegistersConfigurationTest {
    @Test
    public void loadConfigurationWithDefaultRegistersResourceFile() {
        RegistersConfiguration registersConfiguration = new RegistersConfiguration(Optional.empty());

        assertThat(registersConfiguration.getRegister("register").getRegisterName(), equalTo("register"));
    }

   @Test
    public void loadConfigurationWithExternalPathOfRegistersResourceFile() throws URISyntaxException {
       @SuppressWarnings("ConstantConditions")
       String absolutePath = new File(this.getClass().getClassLoader().getResource("config/registers.yaml").toURI()).getAbsolutePath();

       RegistersConfiguration registersConfiguration = new RegistersConfiguration(Optional.of(absolutePath));

        assertThat(registersConfiguration.getRegister("register").getRegisterName(), equalTo("register"));
    }

}
