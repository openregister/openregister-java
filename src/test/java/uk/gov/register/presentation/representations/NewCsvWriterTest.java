package uk.gov.register.presentation.representations;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.view.NewEntryListView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;


public class NewCsvWriterTest {
    @Test
    public void writes_NewEntryListView_to_output_stream() throws IOException {
        NewCsvWriter csvWriter = new NewCsvWriter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        csvWriter.writeTo(new NewEntryListView(null,null,null, Optional.empty(),
                ImmutableList.of(new Entry("1","1234abcd",new Timestamp(1400000000000L)))),
                NewEntryListView.class,
                null,
                null,
                null,
                null,
                outputStream);
        byte[] bytes = outputStream.toByteArray();
        String generatedCsv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(generatedCsv, is("entry-number,item-hash,entry-timestamp\r\n1,sha-256:1234abcd,2014-05-13T16:53:20Z\r\n"));
    }
}