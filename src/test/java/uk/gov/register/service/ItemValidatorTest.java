package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.RegisterConfigConfiguration;
import uk.gov.register.core.RegisterName;
import uk.gov.register.exceptions.ItemValidationException;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ItemValidatorTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private RegisterConfigConfiguration registerConfigConfiguration;
    private ConfigManager configManager;
    private ItemValidator itemValidator;

    @Before
    public void setup() {
        registerConfigConfiguration = mock(RegisterConfigConfiguration.class);
        when(registerConfigConfiguration.getDownloadConfigs()).thenReturn(true);

        configManager = new ConfigManager(registerConfigConfiguration, Optional.empty(), Optional.empty());
        itemValidator = new ItemValidator(configManager, new RegisterName("register"));
    }

    @Test
    public void validateItem_throwsValidationException_givenPrimaryKeyOfRegisterNotExists() throws IOException {
        String jsonString = "{\"text\":\"bar\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            itemValidator.validateItem(jsonNode);
            fail("Must not execute this statement");
        } catch (ItemValidationException e) {
            assertThat(e.getMessage(), equalTo("Entry does not contain primary key field 'register'"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateItem_throwsValidationException_givenPrimaryKeyFieldIsEmpty() throws IOException {
        String jsonString = "{\"register\":\"  \",\"text\":\"bar\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            itemValidator.validateItem(jsonNode);
            fail("Must not execute this statement");
        } catch (ItemValidationException e) {
            assertThat(e.getMessage(), equalTo("Primary key field 'register' must have a valid value"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateItem_throwsValidationException_givenFieldValueIsNotOfCorrectDatatypeType() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"text\":5}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            itemValidator.validateItem(jsonNode);
            fail("Must not execute this statement");
        } catch (ItemValidationException e) {
            assertThat(e.getMessage(), equalTo("Field 'text' value must be of type 'text'"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateItem_throwsValidationException_givenEntryContainsUnknownFields() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"text\":5,\"key1\":\"value\",\"key2\":\"value\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            itemValidator.validateItem(jsonNode);
            fail("Must not execute this statement");
        } catch (ItemValidationException e) {
            assertThat(e.getMessage(), equalTo("Entry contains invalid fields: [key1, key2]"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateItem_throwsValidationException_givenFieldWithCardinalityManyHasNonArrayValue() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"fields\":\"nonAcceptableNonArrayFieldValue\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            itemValidator.validateItem(jsonNode);
            fail("Must not execute this statement");
        } catch (ItemValidationException e) {
            assertThat(e.getMessage(), equalTo("Field 'fields' has cardinality 'n' so the value must be an array of 'string'"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateItem_throwsValidationException_givenFieldWithCardinalityManyHasNonMatchedDatatypeValues() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"fields\":[\"foo\",5]}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            itemValidator.validateItem(jsonNode);
            fail("Must not execute this statement");
        } catch (ItemValidationException e) {
            assertThat(e.getMessage(), equalTo("Field 'fields' values must be of type 'string'"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void noErrorWhenEntryIsValid() throws IOException, ItemValidationException {
        String jsonString = "{\"register\":\"aregister\",\"text\":\"some text\"}";
        itemValidator.validateItem(nodeOf(jsonString));
    }

    private JsonNode nodeOf(String jsonString) throws IOException {
        return objectMapper.readValue(jsonString, JsonNode.class);
    }

    @Test
    public void validateItem_failsOnEmptyStringField() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"fields\":[\"foo\",\"\"]}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            itemValidator.validateItem(jsonNode);
            fail("Must not execute this statement");
        } catch (ItemValidationException e) {
            assertThat(e.getMessage(), equalTo("Field 'fields' values must be of type 'string'"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }
}
