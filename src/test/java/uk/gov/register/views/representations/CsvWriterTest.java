package uk.gov.register.views.representations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import uk.gov.register.core.*;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.EntryListView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.RecordView;
import uk.gov.register.views.RecordsView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CsvWriterTest {

    CsvWriter csvWriter = new CsvWriter();
    private final ImmutableList<Field> fields = ImmutableList.of(
            new Field("key1", "datatype", new RegisterName("register"), Cardinality.ONE, "text"),
            new Field("key2", "datatype", new RegisterName("register"), Cardinality.ONE, "text"),
            new Field("key3", "datatype", new RegisterName("register"), Cardinality.ONE, "text"),
            new Field("key4", "datatype", new RegisterName("register"), Cardinality.ONE, "text"));

    @Test
    public void writes_EntryListView_to_output_stream() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        csvWriter.writeTo(new EntryListView(
                        ImmutableList.of(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "1234abcd"), Instant.ofEpochSecond(1400000000L), "abc", EntryType.user))),
                EntryListView.class,
                null,
                null,
                null,
                null,
                outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("index-entry-number,entry-number,entry-timestamp,key,item-hash\r\n1,1,2014-05-13T16:53:20Z,abc,sha-256:1234abcd\r\n"));
    }

    @Test
    public void writeEntriesTo_writesCsvEscapedEntries() throws IOException {

        ImmutableMap<String, FieldValue> fieldValueMap = ImmutableMap.of(
                "key1", new StringValue("valu\te1"),
                "key2", new StringValue("val,ue2"),
                "key3", new StringValue("val\"ue3"),
                "key4", new StringValue("val\nue4")
        );
        ItemView itemView = new ItemView(null, fieldValueMap, fields);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        csvWriter.writeTo(itemView, itemView.getClass(), null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1,key2,key3,key4\r\n\"valu\te1\",\"val,ue2\",\"val\"\"ue3\",\"val\nue4\"\r\n"));
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

        csvWriter.writeTo(itemView, itemView.getClass(), null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1,key2,key3,key4\r\nvalue1;value2;value3,value4;value5;value6,,\r\n"));
    }

    @Test
    public void writeEntriesTo_includesAllColumnsEvenWhenValuesAreNotPresent() throws Exception {

        ImmutableMap<String, FieldValue> fieldValueMap = ImmutableMap.of(
                "key1", new StringValue("value1"),
                "key2", new StringValue(""),
                "key3", new StringValue(""),
                "key4", new StringValue("")
        );
        ItemView itemView = new ItemView(null, fieldValueMap, fields);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        csvWriter.writeTo(itemView, itemView.getClass(), null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1,key2,key3,key4\r\nvalue1,,,\r\n"));
    }

    @Test
    public void writeEntriesTo_writeRecordView() throws Exception {
        ObjectMapper objectMapper = Jackson.newObjectMapper();

        Entry entry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), Instant.ofEpochSecond(1470403440), "key1", EntryType.user);
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "aaa"), objectMapper.readTree("{\"key1\":\"item1\"}"));
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "bbb"), objectMapper.readTree("{\"key1\":\"item2\"}"));
        Record record = new Record(entry, Arrays.asList(item, item2));

        ImmutableMap<String, Field> registerFieldsByName = ImmutableMap.of(
                "key1", new Field("key1", "datatype", new RegisterName("address"), Cardinality.ONE, "text"),
                "key2", new Field("key2", "datatype", new RegisterName("address"), Cardinality.ONE, "text"),
                "key3", new Field("key3", "datatype", new RegisterName("address"), Cardinality.ONE, "text"),
                "key4", new Field("key4", "datatype", new RegisterName("address"), Cardinality.ONE, "text"));
        Map<String, Field> metadataFieldsByName = mock(Map.class);

        ItemConverter itemConverter = mock(ItemConverter.class);
        when(itemConverter.convertItem(item, registerFieldsByName)).thenReturn(ImmutableMap.of("key1", new StringValue("item1")));
        when(itemConverter.convertItem(item2, registerFieldsByName)).thenReturn(ImmutableMap.of("key1", new StringValue("item2")));

        RecordView recordView = new RecordView(record, registerFieldsByName, metadataFieldsByName, itemConverter);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        csvWriter.writeTo(recordView, recordView.getClass(), null, null, null, null, outputStream);

        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);

        String expected = "index-entry-number,entry-number,entry-timestamp,key,key1,key2,key3,key4\r\n" +
                "1,1,2016-08-05T13:24:00Z,key1,item1,,,\r\n" +
                "1,1,2016-08-05T13:24:00Z,key1,item2,,,\r\n";

        assertThat(generatedCsv, is(expected));
    }

    @Test
    public void writeEntriesTo_writeRecordsView() throws Exception {
        ObjectMapper objectMapper = Jackson.newObjectMapper();

        Instant t1 = Instant.parse("2016-03-29T08:59:25Z");
        Instant t2 = Instant.parse("2016-03-28T09:49:26Z");

        Entry entry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), t1, "123", EntryType.user);
        Entry entry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "cd"), t2, "456", EntryType.user);
        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "ab"), objectMapper.readTree("{\"address\":\"123\",\"street\":\"foo\"}"));
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "cd"), objectMapper.readTree("{\"address\":\"456\",\"street\":\"bar\"}"));
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "ef"), objectMapper.readTree("{\"address\":\"456\",\"street\":\"baz\"}"));
        Record record1 = new Record(entry1, Arrays.asList(item1));
        Record record2 = new Record(entry2, Arrays.asList(item2, item3));

        ItemConverter itemConverter = mock(ItemConverter.class);

        ImmutableMap<String,Field> registerFieldsByName = ImmutableMap.of(
                "address",new Field("address", "datatype", new RegisterName("address"), Cardinality.ONE, "text"),
                "street", new Field("street", "datatype", new RegisterName("address"), Cardinality.ONE, "text"));
        Map<String, Field> metadataFieldsByName = mock(Map.class);

        when(itemConverter.convertItem(item1, registerFieldsByName)).thenReturn(ImmutableMap.of("address", new StringValue("123"),
                "street", new StringValue("foo")));
        when(itemConverter.convertItem(item2, registerFieldsByName)).thenReturn(ImmutableMap.of("address", new StringValue("456"),
                "street", new StringValue("bar")));
        when(itemConverter.convertItem(item3, registerFieldsByName)).thenReturn(ImmutableMap.of("address", new StringValue("456"),
                "street", new StringValue("baz")));


        RecordsView recordsView = new RecordsView(Arrays.asList(record1, record2), registerFieldsByName, metadataFieldsByName, itemConverter, false, false);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        csvWriter.writeTo(recordsView, recordsView.getClass(), null, null, null, null, outputStream);

        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);

        String expected = "index-entry-number,entry-number,entry-timestamp,key,address,street\r\n" +
                "1,1,2016-03-29T08:59:25Z,123,123,foo\r\n" +
                "2,2,2016-03-28T09:49:26Z,456,456,bar\r\n" +
                "2,2,2016-03-28T09:49:26Z,456,456,baz\r\n";

        assertThat(generatedCsv, is(expected));
    }
}
