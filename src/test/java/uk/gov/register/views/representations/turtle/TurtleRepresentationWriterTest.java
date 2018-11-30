package uk.gov.register.views.representations.turtle;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import uk.gov.register.core.*;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.EntryView;
import uk.gov.register.views.ItemView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class TurtleRepresentationWriterTest {
    private final RegisterResolver registerResolver = register -> URI.create("http://" + register + ".test.register.gov.uk");

    private final Set<Field> fields = ImmutableSet.of(
            new Field("address", "datatype", new RegisterId("register"), Cardinality.ONE, "text"),
            new Field("location", "datatype", new RegisterId("register"), Cardinality.ONE, "text"),
            new Field("link-values", "datatype", new RegisterId("register"), Cardinality.ONE, "text"),
            new Field("string-values", "datatype", new RegisterId("register"), Cardinality.ONE, "text"),
            new Field("website", "datatype", new RegisterId("register"), Cardinality.ONE, "text"));

    @Test
    public void rendersFieldPrefixFromConfiguration() throws Exception {
        Map<String, FieldValue> map = ImmutableMap.of(
                "key1", new StringValue("value1"),
                "key2", new StringValue("value2"),
                "key3", new StringValue("val\"ue3"),
                "key4", new StringValue("value4")
        );
        ItemView itemView = new ItemView(new HashValue(HashingAlgorithm.SHA256, "hash"), map, fields);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ItemTurtleWriter writer = new ItemTurtleWriter(() -> new RegisterId("address"), registerResolver);
        writer.writeTo(itemView, itemView.getClass(), null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedTtl, containsString("@prefix field:   <http://field.test.register.gov.uk/records/> ."));
    }

    @Test
    public void rendersEntryIdentifierFromRequestContext() throws Exception {
        Entry entry = new Entry(52, new HashValue(HashingAlgorithm.SHA256, "hash"), new HashValue(HashingAlgorithm.SHA256, "blob-hash"), Instant.now(), "key", EntryType.user);
        EntryView entryView = new EntryView(entry);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        EntryTurtleWriter writer = new EntryTurtleWriter(() -> new RegisterId("address"), registerResolver);
        writer.writeTo(entryView, entryView.getClass(), null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedTtl, containsString("<http://address.test.register.gov.uk/entries/52>"));
    }

    @Test
    public void rendersLinksCorrectlyAsUrls() throws Exception {
        Map<String, FieldValue> map =
                ImmutableMap.of(
                        "address", new RegisterLinkValue(new RegisterId("address"), "1111111"),
                        "location", new RegisterLinkValue(new RegisterId("address"), "location-value"),
                        "name", new StringValue("foo")
                );

        ItemView itemView = new ItemView(new HashValue(HashingAlgorithm.SHA256, "itemhash"), map, fields);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ItemTurtleWriter writer = new ItemTurtleWriter(() -> new RegisterId("address"), registerResolver);
        writer.writeTo(itemView, itemView.getClass(), null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedTtl, containsString("field:address <http://address.test.register.gov.uk/records/1111111>"));
        assertThat(generatedTtl, containsString("field:location <http://address.test.register.gov.uk/records/location-value>"));
        assertThat(generatedTtl, containsString("field:name \"foo\""));
        assertThat(generatedTtl, containsString("<http://address.test.register.gov.uk/items/sha-256:itemhash>"));
    }

    @Test
    public void rendersLists() throws Exception {
        Map<String, FieldValue> map =
                ImmutableMap.of(
                        "link-values", new ListValue(asList(new RegisterLinkValue(new RegisterId("address"), "1111111"), new RegisterLinkValue(new RegisterId("address"), "2222222"))),
                        "string-values", new ListValue(asList(new StringValue("value1"), new StringValue("value2"))),
                        "name", new StringValue("foo")
                );

        ItemView itemView = new ItemView(new HashValue(HashingAlgorithm.SHA256, "hash"), map, fields);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ItemTurtleWriter writer = new ItemTurtleWriter(() -> new RegisterId("address"), registerResolver);
        writer.writeTo(itemView, itemView.getClass(), null, null, null, null, outputStream);

        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);

        assertThat(generatedTtl, containsString("field:link-values <http://address.test.register.gov.uk/records/1111111> , <http://address.test.register.gov.uk/records/2222222>"));
        assertThat(generatedTtl, containsString("field:string-values \"value2\" , \"value1\""));
        assertThat(generatedTtl, containsString("field:name \"foo\""));
    }

    @Test
    public void rendersUrlValuesAsResources() throws IOException {
        Map<String, FieldValue> map =
                ImmutableMap.of(
                        "name", new StringValue("foo"),
                        "website", new UrlValue("https://www.gov.uk")
                );

        ItemView itemView = new ItemView(new HashValue(HashingAlgorithm.SHA256, "hash"), map, fields);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ItemTurtleWriter writer = new ItemTurtleWriter(() -> new RegisterId("government-organisation"), registerResolver);
        writer.writeTo(itemView, itemView.getClass(), null, null, null, null, outputStream);

        byte[] bytes = outputStream.toByteArray();
        String generatedTtl = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedTtl, containsString("field:website <https://www.gov.uk>"));
    }
}
