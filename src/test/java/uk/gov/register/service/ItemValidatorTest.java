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

    private final ObjectMapper objectMapper = new ObjectMapper();

    //@Mock
    //private ConfigManager configManager;

    @Mock
    private RegisterMetadata registerMetadata;

    //@Mock
    //private RegistersConfiguration registersConfiguration;

    //@Mock
    //private FieldsConfiguration fieldsConfiguration;

    private Map<String,Field> fieldsByName;

    private ItemValidator itemValidator;

    @Before
    public void setup() throws NoSuchConfigException, IOException {
        MockitoAnnotations.initMocks(this);
        RegisterName registerName = new RegisterName("register");

       // when(configManager.getRegistersConfiguration()).thenReturn(registersConfiguration);
       // when(configManager.getFieldsConfiguration()).thenReturn(fieldsConfiguration);
       // when(registersConfiguration.getRegisterMetadata(any(RegisterName.class))).thenReturn(registerMetadata);
        when(registerMetadata.getRegisterName()).thenReturn(registerName);

        when(registerMetadata.getFields()).thenReturn(Arrays.asList("register", "text", "registry", "phase", "copyright", "fields"));
        Field textField = new Field("text", "text", registerName, Cardinality.ONE, "text text");
        Field registerField = new Field("register", "text", registerName, Cardinality.ONE, "register text");
        Field fieldsField = new Field("fields", "string", registerName, Cardinality.MANY, "fields text");

        fieldsByName = ImmutableMap.of("text", textField, "register", registerField, "fields", fieldsField);

//        when(fieldsConfiguration.getField("text")).thenReturn(textField);
//        when(fieldsConfiguration.getField("register")).thenReturn(registerField);
//        when(fieldsConfiguration.getField("fields")).thenReturn(fieldsField);

        itemValidator = new ItemValidator(new RegisterName("register"));
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

}
