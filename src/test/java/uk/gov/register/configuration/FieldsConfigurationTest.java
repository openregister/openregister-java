package uk.gov.register.configuration;

import io.dropwizard.testing.ResourceHelpers;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class FieldsConfigurationTest {
    @Test
    public void loadConfigurationWithDefaultFieldsResourceFile() {
        FieldsConfiguration fieldsConfiguration = new FieldsConfiguration("src/main/resources/config/fields.yaml");

        assertThat(fieldsConfiguration.getField("register").get().fieldName, equalTo("register"));
    }

    @Test
    public void loadConfigurationWithExternalPathOfFieldsResourceFile() throws URISyntaxException {
        @SuppressWarnings("ConstantConditions")
        String fileUrl = ResourceHelpers.resourceFilePath("config/fields.yaml");

        FieldsConfiguration fieldsConfiguration = new FieldsConfiguration(fileUrl);

        assertThat(fieldsConfiguration.getField("register").get().fieldName, equalTo("register"));
    }
}