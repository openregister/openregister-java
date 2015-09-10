package uk.gov.register.presentation.representations;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.RecordView;
import uk.gov.register.presentation.StringValue;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TsvWriterTest {
    @Test
    public void tsv_entriesAreGenerated() throws IOException {
        TsvWriter writer = new TsvWriter();


        Map<String, FieldValue> entryMap = ImmutableMap.of(
                "key1", new StringValue("value1"),
                "key2", new StringValue("value2"),
                "key3", new StringValue("val\"ue3"),
                "key4", new StringValue("value4")
        );
        RecordView record = new RecordView("hash1", "registerName", entryMap);

        TestOutputStream entityStream = new TestOutputStream();


        writer.writeRecordsTo(entityStream, Collections.singletonList(record));

        assertThat(entityStream.contents, equalTo("hash\tkey1\tkey2\tkey3\tkey4\nhash1\tvalue1\tvalue2\tval\"ue3\tvalue4\n"));
    }

}
