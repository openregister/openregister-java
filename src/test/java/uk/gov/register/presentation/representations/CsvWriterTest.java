package uk.gov.register.presentation.representations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.StringValue;
import uk.gov.register.presentation.config.Register;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CsvWriterTest {
    @Test
    public void writeEntriesTo_writesCsvEscapedEntries() throws IOException {
        CsvWriter csvWriter = new CsvWriter();
        Map<String, FieldValue> entryMap =
                ImmutableMap.of(
                        "key1", new StringValue("valu\te1"),
                        "key2", new StringValue("val,ue2"),
                        "key3", new StringValue("val\"ue3"),
                        "key4", new StringValue("val\nue4")
                );


        EntryView entry = new EntryView(52, "hash1", "registerName", entryMap);

        TestOutputStream entityStream = new TestOutputStream();

        csvWriter.writeEntriesTo(entityStream, new Register("registerName", ImmutableSet.of("key1", "key2", "key3", "key4"), "", null, ""), Collections.singletonList(entry));

        assertThat(entityStream.contents, equalTo("entry,key1,key2,key3,key4\r\n52,valu\te1,\"val,ue2\",\"val\"\"ue3\",\"val\nue4\"\r\n"));
    }
}
