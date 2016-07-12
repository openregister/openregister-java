package uk.gov.register.presentation.representations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.register.presentation.ListValue;
import uk.gov.register.presentation.StringValue;
import uk.gov.register.presentation.config.Register;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.view.ItemView;
import uk.gov.register.presentation.view.EntryListView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CsvWriterTest {

    CsvWriter csvWriter = new CsvWriter();
    Register register = new Register("register1", ImmutableList.of("key1", "key2", "key3", "key4"),
            "copyright", "registry1", "text1", "phase1");

    @Test
    public void writes_EntryListView_to_output_stream() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        csvWriter.writeTo(new EntryListView(null,null,null, Optional.empty(),
                ImmutableList.of(new Entry("1","1234abcd", Instant.ofEpochSecond(1400000000L)))),
                EntryListView.class,
                null,
                null,
                null,
                null,
                outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("entry-number,entry-timestamp,item-hash\r\n1,2014-05-13T16:53:20Z,sha-256:1234abcd\r\n"));
    }

    @Test
    public void writeEntriesTo_writesCsvEscapedEntries() throws IOException {
        ItemView itemView = mock(ItemView.class);

        when(itemView.getContent()).thenReturn(ImmutableMap.of(
                "key1", new StringValue("valu\te1"),
                "key2", new StringValue("val,ue2"),
                "key3", new StringValue("val\"ue3"),
                "key4", new StringValue("val\nue4")
        ));
        when(itemView.getRegister()).thenReturn(register);
        when(itemView.csvRepresentation()).thenCallRealMethod();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        csvWriter.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1,key2,key3,key4\r\n\"valu\te1\",\"val,ue2\",\"val\"\"ue3\",\"val\nue4\"\r\n"));
    }

    @Test
    public void writeEntriesTo_writesLists() throws IOException {
        ItemView itemView = mock(ItemView.class);

        when(itemView.getContent()).thenReturn(ImmutableMap.of("key1",
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
                )));
        when(itemView.getRegister()).thenReturn(register);
        when(itemView.csvRepresentation()).thenCallRealMethod();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        csvWriter.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1,key2,key3,key4\r\nvalue1;value2;value3,value4;value5;value6,,\r\n"));
    }

    @Test
    public void writeEntriesTo_includesAllColumnsEvenWhenValuesAreNotPresent() throws Exception {
        ItemView itemView = mock(ItemView.class);

        when(itemView.getContent()).thenReturn(ImmutableMap.of(
                "key1", new StringValue("value1"),
                "key2", new StringValue(""),
                "key3", new StringValue(""),
                "key4", new StringValue("")
        ));
        when(itemView.getRegister()).thenReturn(register);
        when(itemView.csvRepresentation()).thenCallRealMethod();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        csvWriter.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1,key2,key3,key4\r\nvalue1,,,\r\n"));
    }
}
