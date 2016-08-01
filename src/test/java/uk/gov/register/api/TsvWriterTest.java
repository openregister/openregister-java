package uk.gov.register.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.register.views.representations.TsvWriter;
import uk.gov.register.core.ListValue;
import uk.gov.register.presentation.StringValue;
import uk.gov.register.configuration.Register;
import uk.gov.register.views.ItemView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TsvWriterTest {

    private final TsvWriter writer = new TsvWriter();
    Register register = new Register("register1", ImmutableList.of("key1", "key2", "key3", "key4"),
            "copyright", "registry1", "text1", "phase1");

    @Test
    public void tsv_entriesAreGenerated() throws IOException {
        ItemView itemView = mock(ItemView.class);

        when(itemView.getContent()).thenReturn(ImmutableMap.of(
                "key1", new StringValue("value1"),
                "key2", new StringValue("value2"),
                "key3", new StringValue("val\"ue3"),
                "key4", new StringValue("value4")
        ));
        when(itemView.getRegister()).thenReturn(register);
        when(itemView.csvRepresentation()).thenCallRealMethod();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        writer.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1\tkey2\tkey3\tkey4\nvalue1\tvalue2\tval\"ue3\tvalue4\n"));
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

        writer.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1\tkey2\tkey3\tkey4\nvalue1;value2;value3\tvalue4;value5;value6\t\t\n"));
    }

    @Test
    public void tsvWriter_rendersAllFieldsEvenIfSomeAreMissing() throws Exception {
        ItemView itemView = mock(ItemView.class);

        when(itemView.getContent()).thenReturn(ImmutableMap.of(
                "key1", new StringValue("value1")
        ));
        when(itemView.getRegister()).thenReturn(register);
        when(itemView.csvRepresentation()).thenCallRealMethod();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        writer.writeTo(itemView, ItemView.class, null, null, null, null, outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("key1\tkey2\tkey3\tkey4\nvalue1\t\t\t\n"));
    }
}
