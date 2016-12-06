package uk.gov.register.views.representations.turtle;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.configuration.FieldsConfiguration;
import uk.gov.register.core.*;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.EntryView;
import uk.gov.register.views.ItemView;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TurtleRepresentationWriterTest {
    private final RegisterResolver registerResolver = register -> URI.create("http://" + register + ".test.register.gov.uk");
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

        itemConverter = new ItemConverter(fieldsConfiguration);
    }

    @Test
    public void rendersFieldPrefixFromConfiguration() throws Exception {
        Map<String, FieldValue> map = ImmutableMap.of(
                "key1", new StringValue("value1"),
                "key2", new StringValue("value2"),
                "key3", new StringValue("val\"ue3"),
                "key4", new StringValue("value4")
        );
        ItemView itemView = new ItemView(requestContext, null, null, itemConverter, new Item(new HashValue(HashingAlgorithm.SHA256, "hash"), objectMapper.valueToTree(map)), () -> Optional.empty(), registerResolver, new EmptyRegister());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ItemTurtleWriter writer = new ItemTurtleWriter(requestContext, () -> "address", registerResolver);
        writer.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedTtl, containsString("@prefix field:   <http://field.test.register.gov.uk/record/> ."));
    }

    @Test
    public void rendersEntryIdentifierFromRequestContext() throws Exception {
        EntryView entryView = new EntryView(requestContext, null, null, new Entry(52, new HashValue(HashingAlgorithm.SHA256, "hash"), Instant.now(), "key"), () -> Optional.empty(), registerResolver, new EmptyRegister());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        EntryTurtleWriter writer = new EntryTurtleWriter(requestContext, () -> "address", registerResolver);
        writer.writeTo(entryView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedTtl, containsString("<http://address.test.register.gov.uk/entry/52>"));
    }

    @Test
    public void rendersLinksCorrectlyAsUrls() throws Exception {
        Map<String, FieldValue> map =
                ImmutableMap.of(
                        "address", new LinkValue("address", "1111111"),
                        "location", new StringValue("location-value"),
                        "name", new StringValue("foo")
                );

        ItemView itemView = new ItemView(requestContext, null, null, itemConverter, new Item(new HashValue(HashingAlgorithm.SHA256, "itemhash"), objectMapper.valueToTree(map)), () -> Optional.empty(), registerResolver, new EmptyRegister());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ItemTurtleWriter writer = new ItemTurtleWriter(requestContext, () -> "address", registerResolver);
        writer.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedTtl, containsString("field:address <http://address.test.register.gov.uk/record/1111111>"));
        assertThat(generatedTtl, containsString("field:location <http://address.test.register.gov.uk/record/location-value>"));
        assertThat(generatedTtl, containsString("field:name \"foo\""));
        assertThat(generatedTtl, containsString("<http://address.test.register.gov.uk/item/sha-256:itemhash>"));
    }

    @Test
    public void rendersLists() throws Exception {
        Map<String, FieldValue> map =
                ImmutableMap.of(
                        "link-values", new ListValue(asList(new LinkValue("address", "1111111"), new LinkValue("address", "2222222"))),
                        "string-values", new ListValue(asList(new StringValue("value1"), new StringValue("value2"))),
                        "name", new StringValue("foo")
                );

        ItemView itemView = new ItemView(requestContext, null, null, itemConverter, new Item(new HashValue(HashingAlgorithm.SHA256, "hash"), objectMapper.valueToTree(map)), () -> Optional.empty(), registerResolver, new EmptyRegister());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ItemTurtleWriter writer = new ItemTurtleWriter(requestContext, () -> "address", registerResolver);
        writer.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);

        assertThat(generatedTtl, containsString("field:link-values <http://address.test.register.gov.uk/record/1111111> , <http://address.test.register.gov.uk/record/2222222>"));
        assertThat(generatedTtl, containsString("field:string-values \"value2\" , \"value1\""));
        assertThat(generatedTtl, containsString("field:name \"foo\""));
    }
}
