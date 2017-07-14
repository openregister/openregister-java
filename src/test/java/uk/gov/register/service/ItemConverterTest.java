package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.configuration.RegisterConfigConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.exceptions.NoSuchConfigException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ItemConverterTest {

    private ItemConverter itemConverter;

    @Before
    public void setup() throws IOException, NoSuchConfigException {
        itemConverter = new ItemConverter();
    }

    @Test
    public void convert_shouldConvertFieldEntryToStringValue_whenFieldIsNeitherCurieOrHasRegister() {
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.textValue()).thenReturn("Name for the citzens of a country.");
        Map.Entry<String, JsonNode> entry = mock(Map.Entry.class);
        when(entry.getKey()).thenReturn("citizen-names");
        when(entry.getValue()).thenReturn(jsonNode);
        Field citizenNameField = new Field("citizen-names", "string", null, Cardinality.ONE, "Name of the citizens of a country");
        Map<String, Field> fieldsByName = ImmutableMap.of("citizen-names", citizenNameField);

        FieldValue result = itemConverter.convert(entry, fieldsByName);

        assertThat(result, instanceOf(StringValue.class));
        assertThat(result.getValue(), equalTo("Name for the citzens of a country."));
    }

    @Test
    public void convert_shouldConvertEntryToListValue() throws IOException {
        String jsonString = "{\"parent-bodies\":[\"test-1\",\"test-2\"]}";
        JsonNode res = Jackson.newObjectMapper().readValue(jsonString, JsonNode.class);
        Map.Entry<String, JsonNode> entry = mock(Map.Entry.class);
        when(entry.getKey()).thenReturn("parent-bodies");
        when(entry.getValue()).thenReturn(res);
        Field parentBodiesField = new Field("parent-bodies", "string", null, Cardinality.MANY, "Parent Bodies");
        Map<String, Field> fieldsByName = ImmutableMap.of("parent-bodies", parentBodiesField);

        FieldValue result = itemConverter.convert(entry, fieldsByName);

        assertThat(result, instanceOf(ListValue.class));
    }

    @Test
    public void convert_shouldConvertEntryToLinkValue() throws IOException {
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.textValue()).thenReturn("A school in the UK.");
        Map.Entry<String, JsonNode> entry = mock(Map.Entry.class);
        when(entry.getKey()).thenReturn("school");
        when(entry.getValue()).thenReturn(jsonNode);
        Field schoolField = new Field("school", "string", new RegisterName("school"), Cardinality.ONE, "A school in the UK.");
        Map<String, Field> fieldsByName = ImmutableMap.of("school", schoolField);

        FieldValue result = itemConverter.convert(entry, fieldsByName);

        assertThat(result, instanceOf(LinkValue.class));
        assertThat(result.getValue(), equalTo("A school in the UK."));
    }

    @Test
    public void convert_shouldConvertEntryToCurieValue() {
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.textValue()).thenReturn("business:13245");
        Map.Entry<String, JsonNode> entry = mock(Map.Entry.class);
        when(entry.getKey()).thenReturn("business");
        when(entry.getValue()).thenReturn(jsonNode);
        Field businessField = new Field("business", "curie", new RegisterName("company"), Cardinality.ONE, "A Limited Company ...");
        Map<String, Field> fieldsByName = ImmutableMap.of("business", businessField);


        FieldValue result = itemConverter.convert(entry, fieldsByName);

        assertThat(result, instanceOf(LinkValue.CurieValue.class));
        assertThat(result.getValue(), equalTo("business:13245"));
    }
}
