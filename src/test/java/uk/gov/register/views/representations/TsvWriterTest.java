package uk.gov.register.views.representations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.register.core.*;
import uk.gov.register.views.ItemView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TsvWriterTest {

    private final TsvWriter writer = new TsvWriter();
    private final Iterable<Field> fields = ImmutableList.of(
            new Field("key1", "datatype", new RegisterName("register"),Cardinality.ONE, "text"),
            new Field("key2", "datatype", new RegisterName("register"),Cardinality.ONE, "text"),
            new Field("key3", "datatype", new RegisterName("register"),Cardinality.ONE, "text"),
            new Field("key4", "datatype", new RegisterName("register"),Cardinality.ONE, "text"));

    @Test
    public void tsv_entriesAreGenerated() throws IOException {
        ImmutableMap<String, FieldValue> fieldValueMap = ImmutableMap.of(
                "key1", new StringValue("value1"),
                "key2", new StringValue("value2"),
                "key3", new StringValue("val\"ue3"),
                "key4", new StringValue("value4")
        );
        ItemView itemView = new ItemView(null, fieldValueMap, fields);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        writer.writeTo(itemView, itemView.getClass(), null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1\tkey2\tkey3\tkey4\nvalue1\tvalue2\tval\"ue3\tvalue4\n"));
    }

    @Test
    public void writeEntriesTo_writesLists() throws IOException {
        ImmutableMap<String, FieldValue> fieldValueMap = ImmutableMap.of("key1",
                new ListValue(asList(
                        new StringValue("value1"),
                        new StringValue("value2"),
                        new StringValue("value3"))
                ),
                "key2",
                new ListValue(asList(
                        new StringValue("value4"),
                        new StringValue("value5"),
                        new StringValue("value6"))
                ));
        ItemView itemView = new ItemView(null, fieldValueMap, fields);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        writer.writeTo(itemView, itemView.getClass(), null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1\tkey2\tkey3\tkey4\nvalue1;value2;value3\tvalue4;value5;value6\t\t\n"));
    }

    @Test
    public void tsvWriter_rendersAllFieldsEvenIfSomeAreMissing() throws Exception {
        ImmutableMap<String, FieldValue> fieldValueMap = ImmutableMap.of(
                "key1", new StringValue("value1")
        );
        ItemView itemView = new ItemView(null, fieldValueMap, fields);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        writer.writeTo(itemView, itemView.getClass(), null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1\tkey2\tkey3\tkey4\nvalue1\t\t\t\n"));
    }
}
