package uk.gov.register.presentation.representations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.StringValue;
import uk.gov.register.presentation.config.Register;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TsvWriterTest {

    private final TsvWriter writer = new TsvWriter();

    @Test
    public void tsv_entriesAreGenerated() throws IOException {
        EntryView entry = new EntryView(52, "hash1", "registerName", ImmutableMap.of(
                "key1", new StringValue("value1"),
                "key2", new StringValue("value2"),
                "key3", new StringValue("val\"ue3"),
                "key4", new StringValue("value4")
        ));

        TestOutputStream entityStream = new TestOutputStream();

        writer.writeEntriesTo(entityStream, new Register("registerName", ImmutableSet.of("key1", "key2", "key3", "key4"), "", null, ""), Collections.singletonList(entry));

        assertThat(entityStream.contents, equalTo("entry\tkey1\tkey2\tkey3\tkey4\n52\tvalue1\tvalue2\tval\"ue3\tvalue4\n"));
    }

    @Test
    public void tsvWriter_rendersAllFieldsEvenIfSomeAreMissing() throws Exception {
        EntryView entry = new EntryView(52, "hash1", "registerName", ImmutableMap.of(
                "key1", new StringValue("value1")
        ));

        TestOutputStream entityStream = new TestOutputStream();

        writer.writeEntriesTo(entityStream, new Register("registerName", ImmutableSet.of("key1", "key2", "key3", "key4"), "", null, ""), Collections.singletonList(entry));

        assertThat(entityStream.contents, equalTo("entry\tkey1\tkey2\tkey3\tkey4\n52\tvalue1\t\t\t\n"));
    }
}
