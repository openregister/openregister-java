package uk.gov.register.views;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.*;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecordViewTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private RecordView recordView;

    @Before
    public void setup() throws IOException {
        Entry entry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), Instant.ofEpochSecond(1470403440), "b");
        Item item = new Item(new HashValue(HashingAlgorithm.SHA256, "ab"), objectMapper.readTree("{\"a\":\"b\"}"));
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "cd"), objectMapper.readTree("{\"a\":\"d\"}"));
        Record record = new Record(entry, Arrays.asList(item, item2));

        ItemConverter itemConverter = mock(ItemConverter.class);
        when(itemConverter.convertItem(item)).thenReturn(ImmutableMap.of("a", new StringValue("b")));
        when(itemConverter.convertItem(item2)).thenReturn(ImmutableMap.of("a", new StringValue("d")));

        recordView = new RecordView(record, emptyList(), itemConverter);
    }

    @Test
    public void recordJsonRepresentation_isJsonOfEntryAndItemContent() throws JsonProcessingException {
        String result = objectMapper.writeValueAsString(recordView);

        assertThat(result, equalTo("{" +
                "\"b\":{" +
                "\"index-entry-number\":\"1\"," +
                "\"entry-number\":\"1\"," +
                "\"entry-timestamp\":\"2016-08-05T13:24:00Z\"," +
                "\"key\":\"b\"," +
                "\"item\":[{\"a\":\"b\"},{\"a\":\"d\"}]" +
                "}}"));
    }

    @Test
    public void recordJsonRepresentation_isFlatJsonOfEntryAndItemContent() {
        String result = recordView.getFlatRecordsJson().toString();

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
