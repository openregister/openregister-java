package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.register.FieldsConfiguration;
import uk.gov.register.RegistersConfiguration;

import java.io.IOException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class EntryValidatorTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private FieldsConfiguration fieldsConfiguration = new FieldsConfiguration();
    private RegistersConfiguration registerConfiguration = new RegistersConfiguration();

    private EntryValidator entryValidator = new EntryValidator(registerConfiguration, fieldsConfiguration);

    @Test
    public void validateEntry_throwsValidationException_givenPrimaryKeyOfRegisterNotExists() throws IOException {
        String jsonString = "{\"text\":\"bar\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            entryValidator.validateEntry("register", jsonNode);
            fail("Must not execute this statement");
        } catch (EntryValidator.EntryValidationException e) {
            assertThat(e.getMessage(), equalTo("Register's primary key field not available."));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateEntry_throwsValidationException_givenPrimaryKeyOfRegisterIsEmpty() throws IOException {
        String jsonString = "{\"register\":\"\",\"text\":\"bar\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            entryValidator.validateEntry("register", jsonNode);
            fail("Must not execute this statement");
        } catch (EntryValidator.EntryValidationException e) {
            assertThat(e.getMessage(), equalTo("Value for primary key field not exists."));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }

    }

    @Test
    public void validateEntry_throwsValidationException_givenFieldValueIsNotOfCorrectDatatypeType() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"text\":5}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            entryValidator.validateEntry("register", jsonNode);
            fail("Must not execute this statement");
        } catch (EntryValidator.EntryValidationException e) {
            assertThat(e.getMessage(), equalTo("Check field 'text' value, must be of acceptable datatype."));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }

    }

    @Test
    public void validateEntry_throwsValidationException_givenEntryContainsUnknownFields() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"text\":5,\"foo\":\"bar\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            entryValidator.validateEntry("register", jsonNode);
            fail("Must not execute this statement");
        } catch (EntryValidator.EntryValidationException e) {
            assertThat(e.getMessage(), equalTo("Unknown field 'foo'."));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }

    }

    private JsonNode nodeOf(String jsonString) throws IOException {
        return objectMapper.readValue(jsonString, JsonNode.class);
    }
}
