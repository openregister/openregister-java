package uk.gov.register.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.Test;
import uk.gov.register.core.*;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RecordViewTest {
    @Test
    public void recordJsonRepresentation_isFlatJsonOfEntryAndItemContent() throws IOException, JSONException {
        ObjectMapper objectMapper = Jackson.newObjectMapper();

        Entry entry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), Instant.ofEpochSecond(1470403440), "b");
        ItemView itemView = new ItemView(new HashValue(HashingAlgorithm.SHA256, "ab"), ImmutableMap.of("a", new StringValue("b")), emptyList());
        RecordView recordView = new RecordView(entry, itemView, emptyList());

        String result = objectMapper.writeValueAsString(recordView);

        assertThat(result, equalTo("{" +
                "\"entry-number\":\"1\"," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"item-hashes\":[\"sha-256:ab\"]," +
                "\"a\":\"b\"" +
                "}"));
    }
}
