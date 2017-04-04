package uk.gov.register.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
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
    public void recordJsonRepresentation_isJsonOfEntryAndItemContent() throws IOException, JSONException {
        ObjectMapper objectMapper = Jackson.newObjectMapper();

        Entry entry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), Instant.ofEpochSecond(1470403440), "b");
        ItemView itemView = new ItemView(new HashValue(HashingAlgorithm.SHA256, "ab"), ImmutableMap.of("a", new StringValue("b")), emptyList());
        ItemView itemView2 = new ItemView(new HashValue(HashingAlgorithm.SHA256, "cd"), ImmutableMap.of("a", new StringValue("d")), emptyList());
        RecordView recordView = new RecordView(entry, Lists.newArrayList(itemView, itemView2), emptyList());

        String result = objectMapper.writeValueAsString(recordView);

        assertThat(result, equalTo("{" +
                "\"index-entry-number\":\"1\"," +
                "\"entry-number\":\"1\"," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"key\":\"b\"," +
                "\"item\":[{\"a\":\"b\"},{\"a\":\"d\"}]" +
                "}"));
    }

    @Test
    public void recordJsonRepresentation_isFlatJsonOfEntryAndItemContent() throws IOException, JSONException {

        Entry entry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), Instant.ofEpochSecond(1470403440), "b");
        ItemView itemView = new ItemView(new HashValue(HashingAlgorithm.SHA256, "ab"), ImmutableMap.of("a", new StringValue("b")), emptyList());
        ItemView itemView2 = new ItemView(new HashValue(HashingAlgorithm.SHA256, "ad"), ImmutableMap.of("a", new StringValue("d")), emptyList());
        RecordView recordView = new RecordView(entry, Lists.newArrayList(itemView, itemView2), emptyList());

        String result = recordView.getFlatRecordJson().toString();

        assertThat(result, equalTo("[{" +
                "\"index-entry-number\":\"1\"," +
                "\"entry-number\":\"1\"," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"key\":\"b\"," +
                "\"a\":\"b\"}," +
                "{\"index-entry-number\":\"1\"," +
                "\"entry-number\":\"1\"," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"key\":\"b\"," +
                "\"a\":\"d\"" +
                "}]"));
    }
}
