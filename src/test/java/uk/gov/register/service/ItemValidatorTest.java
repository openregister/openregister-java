package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Field;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterName;
import uk.gov.register.exceptions.ItemValidationException;
import uk.gov.register.exceptions.NoSuchConfigException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class ItemValidatorTest {

    @Mock
    private RegisterMetadata registerMetadata;

    @Mock
    private RegisterMetadata countryRegisterMetadata;

    private Map<String,Field> fieldsByName;
    private Map<String,Field> countryFieldsByName;

    private ItemValidator itemValidator;
    private ItemValidator countryItemValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() throws NoSuchConfigException, IOException {
        MockitoAnnotations.initMocks(this);
        RegisterName registerName = new RegisterName("register");
        RegisterName countryRegisterName = new RegisterName("country");

        when(registerMetadata.getRegisterName()).thenReturn(registerName);
        when(countryRegisterMetadata.getRegisterName()).thenReturn(countryRegisterName);

        when(registerMetadata.getFields()).thenReturn(Arrays.asList("register", "text", "registry", "phase", "copyright", "fields"));
        when(countryRegisterMetadata.getFields()).thenReturn(Arrays.asList("country", "start-date"));

        Field textField = new Field("text", "text", registerName, Cardinality.ONE, "text text");
        Field registerField = new Field("register", "text", registerName, Cardinality.ONE, "register text");
        Field fieldsField = new Field("fields", "string", registerName, Cardinality.MANY, "fields text");

        Field countryField = new Field("country", "string", countryRegisterName, Cardinality.ONE, "countryName");
        Field startDateField = new Field("start-date\"", "datetime", null, Cardinality.ONE, "Start date text");

        fieldsByName = ImmutableMap.of("text", textField, "register", registerField, "fields", fieldsField);
        countryFieldsByName = ImmutableMap.of("country", countryField, "start-date", startDateField);

        itemValidator = new ItemValidator(registerName);
        countryItemValidator = new ItemValidator(countryRegisterName);
    }

    @Test
    public void validateItem_throwsValidationException_givenPrimaryKeyOfRegisterNotExists() throws IOException {
        String jsonString = "{\"text\":\"bar\"}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            itemValidator.validateItem(jsonNode, fieldsByName, registerMetadata);
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
            itemValidator.validateItem(jsonNode, fieldsByName, registerMetadata);
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
            itemValidator.validateItem(jsonNode, fieldsByName, registerMetadata);
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
            itemValidator.validateItem(jsonNode, fieldsByName, registerMetadata);
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
            itemValidator.validateItem(jsonNode, fieldsByName, registerMetadata);
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
            itemValidator.validateItem(jsonNode, fieldsByName, registerMetadata);
            fail("Must not execute this statement");
        } catch (ItemValidationException e) {
            assertThat(e.getMessage(), equalTo("Field 'fields' values must be of type 'string'"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void noErrorWhenEntryIsValid() throws IOException, ItemValidationException {
        String jsonString = "{\"register\":\"aregister\",\"text\":\"some text\"}";
        itemValidator.validateItem(nodeOf(jsonString), fieldsByName, registerMetadata);
    }

    private JsonNode nodeOf(String jsonString) throws IOException {
        return objectMapper.readValue(jsonString, JsonNode.class);
    }

    @Test
    public void validateItem_failsOnEmptyStringField() throws IOException {
        String jsonString = "{\"register\":\"aregister\",\"fields\":[\"foo\",\"\"]}";
        JsonNode jsonNode = nodeOf(jsonString);
        try {
            itemValidator.validateItem(jsonNode, fieldsByName, registerMetadata);
            fail("Must not execute this statement");
        } catch (ItemValidationException e) {
            assertThat(e.getMessage(), equalTo("Field 'fields' values must be of type 'string'"));
            assertThat(e.getEntry().toString(), equalTo(jsonString));
        }
    }

    @Test
    public void validateItem_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYY() throws IOException {
        String jsonString = "{\"country\":\"myCountry\",\"start-date\":\"2012\"}";
        JsonNode jsonNode = nodeOf(jsonString);

        try {
            countryItemValidator.validateItem(jsonNode, countryFieldsByName, countryRegisterMetadata);
        } catch (ItemValidationException e) {
            fail("Must not execute this statement");
        }
    }

    @Test
    public void validateItem_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYDDMM() throws IOException {
        String jsonString = "{\"country\":\"myCountry\",\"start-date\":\"2012-04\"}";
        JsonNode jsonNode = nodeOf(jsonString);

        try {
            countryItemValidator.validateItem(jsonNode, countryFieldsByName, countryRegisterMetadata);
        } catch (ItemValidationException e) {
            fail("Must not execute this statement");
        }
    }

    @Test
    public void validateItem_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDD() throws IOException {
        String jsonString = "{\"country\":\"myCountry\",\"start-date\":\"2012-04-01\"}";
        JsonNode jsonNode = nodeOf(jsonString);

        try {
            countryItemValidator.validateItem(jsonNode, countryFieldsByName, countryRegisterMetadata);
        } catch (ItemValidationException e) {
            fail("Must not execute this statement");
        }
    }

    @Test
    public void validateItem_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDDThhmmss() throws IOException {
        String jsonString = "{\"country\":\"myCountry\",\"start-date\":\"2012-04-01T23:23:23\"}";
        JsonNode jsonNode = nodeOf(jsonString);

        try {
            countryItemValidator.validateItem(jsonNode, countryFieldsByName, countryRegisterMetadata);
        } catch (ItemValidationException e) {
            fail("Must not execute this statement");
        }
    }

    @Test
    public void validateItem_shouldValidateSuccessfully_whenInputDateTimeIsOfFormatYYYYMMDDThhmmssZ() throws IOException {
        String jsonString = "{\"country\":\"myCountry\",\"start-date\":\"2012-04-01T23:23:23Z\"}";
        JsonNode jsonNode = nodeOf(jsonString);

        try {
            countryItemValidator.validateItem(jsonNode, countryFieldsByName, countryRegisterMetadata);
        } catch (ItemValidationException e) {
            fail("Must not execute this statement");
        }
    }

    @Test
    public void validateItem_throwsValidationException_whenInputDateTimeIsNotValid() throws IOException {
        String jsonString = "{\"country\":\"myCountry\",\"start-date\":\"18/12/2009\"}";
        JsonNode jsonNode = nodeOf(jsonString);

        try {
            countryItemValidator.validateItem(jsonNode, countryFieldsByName, countryRegisterMetadata);
            fail("Must not execute this statement");
        } catch (ItemValidationException e) {
            assertThat(e.getMessage(), equalTo("Field 'start-date' value must be of type 'datetime'"));
        }
    }

    @Test
    public void validateItem_shouldValidateSuccessfully_whenInputDateTimeIsEmpty() throws IOException {
        String jsonString = "{\"country\":\"myCountry\",\"start-date\":\"\"}";
        JsonNode jsonNode = nodeOf(jsonString);

        try {
            countryItemValidator.validateItem(jsonNode, countryFieldsByName, countryRegisterMetadata);
        } catch (ItemValidationException e) {
            fail("Must not execute this statement");
        }
    }
}
