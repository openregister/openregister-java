package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import uk.gov.register.presentation.mapper.JsonObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CsvWriterTest {
    @Test
    public void csv_entriesAreEscaped() throws IOException {
        CsvWriter writer = new CsvWriter();

        Map jsonMap = ImmutableMap.of(
                "hash", "hash1",
                "entry", ImmutableMap.of(
                        "key1", "valu\te1",
                        "key2", "val,ue2",
                        "key3", "val\"ue3",
                        "key4", "val\nue4"
                )
        );
        JsonNode node = JsonObjectMapper.convert(jsonMap, JsonNode.class);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Type listJsonNodeType = new TypeToken<List<JsonNode>>() {}.getType();
        writer.writeTo(Collections.singletonList(node), List.class, listJsonNodeType, null, ExtraMediaType.TEXT_CSV_TYPE, null, stream);
        String result = stream.toString("utf-8");

        assertThat(result, equalTo("hash,key1,key2,key3,key4\r\nhash1,valu\te1,\"val,ue2\",\"val\"\"ue3\",\"val\nue4\"\r\n"));
    }

}
