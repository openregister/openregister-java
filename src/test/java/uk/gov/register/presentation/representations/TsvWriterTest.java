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

        writer.writeEntriesTo(entityStream, new Register("registerName", ImmutableSet.of("key1", "key2", "key3", "key4"), "", null, ""), Collections.singletonList(entry));

        assertThat(entityStream.contents, equalTo("entry\tkey1\tkey2\tkey3\tkey4\n52\tvalue1\tvalue2\tval\"ue3\tvalue4\n"));
    }

}
