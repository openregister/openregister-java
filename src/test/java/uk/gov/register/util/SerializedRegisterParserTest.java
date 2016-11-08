package uk.gov.register.util;

import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.serialization.RegisterComponents;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;

public class SerializedRegisterParserTest {

    @Test
    public void shouldParseCommands() throws Exception {
        SerializedRegisterParser parser = new SerializedRegisterParser();
        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register.tsv"))
        ) {
            RegisterComponents registerComponents = parser.parseCommands(serializerRegisterStream);
            Set<Item> items = registerComponents.items;
            List<Entry> entries = registerComponents.entries;

            assertThat(items.size(), is(2));

            List<String> contents = items.stream().map(i -> i.getContent().toString()).collect(Collectors.toList());
            assertThat(contents, hasItems(startsWith("{\"address\":\"9AQZJ3M\""), startsWith("{\"address\":\"9AQZJ3K\"")));

            assertThat(entries.size(), is(2));

            Entry entry0 = entries.get(0);
            assertThat(entry0.getEntryNumber(), is(0));

            Entry entry1 = entries.get(1);
            assertThat(entry1.getEntryNumber(), is(1));
        }
    }

    @Test
    public void shouldParseCommandsEscaped() throws Exception {
        SerializedRegisterParser parser = new SerializedRegisterParser();
        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register-escaped.tsv"))
        ) {
            RegisterComponents registerComponents = parser.parseCommands(serializerRegisterStream);
            Set<Item> items = registerComponents.items;
            List<Entry> entries = registerComponents.entries;

            assertThat(items.size(), is(1));
            assertThat(entries.size(), is(1));

            Entry entry = entries.get(0);
            Item item = items.iterator().next();

            assertThat( entry.getSha256hex(), is(item.getSha256hex()));
        }
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenNoHash() throws Exception {
        SerializedRegisterParser parser = new SerializedRegisterParser();
        InputStream serializerRegisterStream = streamString("append-entry\t2016-10-12T17:45:19.757132");
        parser.parseCommands(serializerRegisterStream);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenNoContent() throws Exception {
        SerializedRegisterParser parser = new SerializedRegisterParser();
        InputStream serializerRegisterStream = streamString("add-item");
        parser.parseCommands(serializerRegisterStream);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenNoHashPrefix() throws Exception {
        SerializedRegisterParser parser = new SerializedRegisterParser();
        InputStream serializerRegisterStream = streamString("append-entry\t2016-10-12T17:45:19.757132\tabc123");
        parser.parseCommands(serializerRegisterStream);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenInvalidCommand() throws Exception {
        SerializedRegisterParser parser = new SerializedRegisterParser();
        InputStream serializerRegisterStream = streamString("assert-root-hash\tabc123");
        parser.parseCommands(serializerRegisterStream);
    }

    @Test(expected = DateTimeParseException.class)
    public void shouldFailIfTimestampNotIso() throws Exception {
        SerializedRegisterParser parser = new SerializedRegisterParser();
        InputStream serializerRegisterStream = streamString("append-entry\t20161212\tsha-256:abc123");
        parser.parseCommands(serializerRegisterStream);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldFailIfInvalidJson() throws Exception {
        SerializedRegisterParser parser = new SerializedRegisterParser();
        InputStream serializerRegisterStream = streamString("add-item\t{\"address\":\"9AQZJ3K\"");
        parser.parseCommands(serializerRegisterStream);
    }


    private InputStream streamString(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }


}
