package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.mapper.JsonObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TsvWriterTest {
    @Test
    public void tsv_entriesAreGenerated() throws IOException {
        TsvWriter writer = new TsvWriter();


        Map entryMap = ImmutableMap.of(
                "key1", "value1",
                "key2", "value2",
                "key3", "val\"ue3",
                "key4", "value4"
        );
        Record record = new Record("hash1", JsonObjectMapper.convert(entryMap, new TypeReference<JsonNode>(){}));

        TestOutputStream entityStream = new TestOutputStream();


        writer.writeRecordsTo(entityStream, Collections.singletonList(record));

        assertThat(entityStream.contents, equalTo("hash\tkey1\tkey2\tkey3\tkey4\nhash1\tvalue1\tvalue2\tval\"ue3\tvalue4\n"));
    }

}
