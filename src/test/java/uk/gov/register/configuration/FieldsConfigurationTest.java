package uk.gov.register.configuration;

import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;
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
        String fileUrl = Paths.get("src/main/resources/config","fields.yaml").toUri().toString();

        FieldsConfiguration fieldsConfiguration = new FieldsConfiguration(Optional.of(fileUrl));

        assertThat(fieldsConfiguration.getField("register").fieldName, equalTo("register"));
    }


}
