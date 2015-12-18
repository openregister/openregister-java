package uk.gov.register;

import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class FieldsConfigurationTest {
    @Test
    public void loadConfigurationWithDefaultFieldsResourceFile() {
        FieldsConfiguration fieldsConfiguration = new FieldsConfiguration(Optional.empty());

        assertThat(fieldsConfiguration.getField("register").fieldName, equalTo("register"));
    }

    @Test
    public void loadConfigurationWithExternalPathOfFieldsResourceFile() throws URISyntaxException {
        @SuppressWarnings("ConstantConditions")
        String absolutePath = new File(this.getClass().getClassLoader().getResource("config/fields.yaml").toURI()).getAbsolutePath();

        FieldsConfiguration fieldsConfiguration = new FieldsConfiguration(Optional.of(absolutePath));

        assertThat(fieldsConfiguration.getField("register").fieldName, equalTo("register"));
    }


}
