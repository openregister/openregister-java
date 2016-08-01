package uk.gov.register.api;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.EntryView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.representations.turtle.EntryTurtleWriter;
import uk.gov.register.views.representations.turtle.ItemTurtleWriter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TurtleRepresentationWriterTest {
    private RequestContext requestContext;
    private ItemConverter itemConverter;
    private ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    private Map<String, Field> fieldsConfigurationMap = ImmutableMap.of(
            "address", new Field("address", "curie", "address", Cardinality.ONE, ""),
            "location", new Field("location", "", "address", Cardinality.ONE, ""),
            "link-values", new Field("link-values", "", "address", Cardinality.MANY, ""),
            "string-values", new Field("string-values", "", "", Cardinality.MANY, "")
            );

    @Before
    public void setUp() throws Exception {
        requestContext = new RequestContext() {
            @Override
            public String getScheme() { return "http"; }
        };

        FieldsConfiguration fieldsConfiguration = mock(FieldsConfiguration.class);
        when(fieldsConfiguration.getField(anyString())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String fieldName = (String) args[0];
            return fieldsConfigurationMap.containsKey(fieldName)
                    ? fieldsConfigurationMap.get(fieldName) : new Field(fieldName, "", "", Cardinality.ONE, "");
        });

        itemConverter = new ItemConverter(fieldsConfiguration, requestContext, () -> "test.register.gov.uk");
    }

    @Test
    public void rendersFieldPrefixFromConfiguration() throws Exception {
        Map<String, FieldValue> map = ImmutableMap.of(
                "key1", new StringValue("value1"),
                "key2", new StringValue("value2"),
                "key3", new StringValue("val\"ue3"),
                "key4", new StringValue("value4")
        );
        ItemView itemView = new ItemView(requestContext, null, null, itemConverter, new Item("hash", objectMapper.valueToTree(map)), () -> "test.register.gov.uk", null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ItemTurtleWriter writer = new ItemTurtleWriter(requestContext, () -> "test.register.gov.uk", () -> "address");
        writer.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedTtl, containsString("@prefix field:   <http://field.test.register.gov.uk/record/> ."));
    }

    @Test
    public void rendersEntryIdentifierFromRequestContext() throws Exception {
        EntryView entryView = new EntryView(requestContext, null, null, new Entry("52", "hash", Instant.now()), () -> "test.register.gov.uk", null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        EntryTurtleWriter writer = new EntryTurtleWriter(requestContext, () -> "test.register.gov.uk", () -> "address");
        writer.writeTo(entryView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedTtl, containsString("<http://address.test.register.gov.uk/entry/52>"));
    }

    @Test
    public void rendersLinksCorrectlyAsUrls() throws Exception {
        Map<String, FieldValue> map =
                ImmutableMap.of(
                        "address", new LinkValue("address", "test.register.gov.uk", "http", "1111111"),
                        "location", new StringValue("location-value"),
                        "name", new StringValue("foo")
                );

        ItemView itemView = new ItemView(requestContext, null, null, itemConverter, new Item("hash", objectMapper.valueToTree(map)), () -> "test.register.gov.uk", null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ItemTurtleWriter writer = new ItemTurtleWriter(requestContext, () -> "test.register.gov.uk", () -> "address");
        writer.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedTtl, containsString("field:address <http://address.test.register.gov.uk/record/1111111>"));
        assertThat(generatedTtl, containsString("field:location <http://address.test.register.gov.uk/record/location-value>"));
        assertThat(generatedTtl, containsString("field:name \"foo\""));
    }

    @Test
    public void rendersLists() throws Exception {
        Map<String, FieldValue> map =
                ImmutableMap.of(
                        "link-values", new ListValue(asList(new LinkValue("address", "test.register.gov.uk", "http", "1111111"), new LinkValue("address", "test.register.gov.uk", "http", "2222222"))),
                        "string-values", new ListValue(asList(new StringValue("value1"), new StringValue("value2"))),
                        "name", new StringValue("foo")
                );

        ItemView itemView = new ItemView(requestContext, null, null, itemConverter, new Item("hash", objectMapper.valueToTree(map)), () -> "test.register.gov.uk", null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ItemTurtleWriter writer = new ItemTurtleWriter(requestContext, () -> "test.register.gov.uk", () -> "address");
        writer.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);

        assertThat(generatedTtl, containsString("field:link-values <http://address.test.register.gov.uk/record/1111111> , <http://address.test.register.gov.uk/record/2222222>"));
        assertThat(generatedTtl, containsString("field:string-values \"value2\" , \"value1\""));
        assertThat(generatedTtl, containsString("field:name \"foo\""));
    }
}
