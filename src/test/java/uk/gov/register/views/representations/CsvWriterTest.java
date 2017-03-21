package uk.gov.register.views.representations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Test;
import uk.gov.register.core.*;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.EntryListView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.RecordView;
import uk.gov.register.views.RecordsView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
                        ImmutableList.of(new Entry(1, new HashValue(HashingAlgorithm.SHA256, "1234abcd"), Instant.ofEpochSecond(1400000000L), "abc"))),
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
        Entry entry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), Instant.ofEpochSecond(1470403440), "key1");
        ItemView itemView = new ItemView(new HashValue(HashingAlgorithm.SHA256, "aaa"), ImmutableMap.of("key1", new StringValue("item1")), emptyList());
        ItemView itemView2 = new ItemView(new HashValue(HashingAlgorithm.SHA256, "bbb"), ImmutableMap.of("key1", new StringValue("item2")), emptyList());
        RecordView recordView = new RecordView(entry, Lists.newArrayList(itemView, itemView2), fields);

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
        Instant t1 = Instant.parse("2016-03-29T08:59:25Z");
        Instant t2 = Instant.parse("2016-03-28T09:49:26Z");
        ImmutableList<Field> fields = ImmutableList.of(
                new Field("address", "datatype", new RegisterName("address"), Cardinality.ONE, "text"),
                new Field("street", "datatype", new RegisterName("address"), Cardinality.ONE, "text"));

        ItemView itemView1 = new ItemView(new HashValue(HashingAlgorithm.SHA256, "ab"),
                ImmutableMap.of("address", new StringValue("123"), "street", new StringValue("foo")), fields);

        ItemView itemView2 = new ItemView(new HashValue(HashingAlgorithm.SHA256, "cd"),
                ImmutableMap.of("address", new StringValue("456"), "street", new StringValue("bar")),
                fields);

        ItemView itemView3 = new ItemView(new HashValue(HashingAlgorithm.SHA256, "ef"),
                ImmutableMap.of("address", new StringValue("456"), "street", new StringValue("baz")),
                fields);

        List<RecordView> records = Lists.newArrayList(
                new RecordView(
                        new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), t1, "123"),
                        Lists.newArrayList(itemView1), fields
                ),
                new RecordView(
                        new Entry(2, new HashValue(HashingAlgorithm.SHA256, "cd"), t2, "456"),
                        Lists.newArrayList(itemView2, itemView3), fields
                )
        );

        RecordsView recordsView = new RecordsView(records, fields);

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
