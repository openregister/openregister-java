package uk.gov.register.presentation.representations;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.EntryView;
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
        EntryView entry = new EntryView(52, "hash1", "registerName", entryMap);

        TestOutputStream entityStream = new TestOutputStream();


        writer.writeEntriesTo(entityStream, Collections.singletonList(entry));

        assertThat(entityStream.contents, equalTo("hash\tkey1\tkey2\tkey3\tkey4\nhash1\tvalue1\tvalue2\tval\"ue3\tvalue4\n"));
    }

}
