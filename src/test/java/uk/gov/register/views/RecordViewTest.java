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
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecordViewTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private RecordView recordView;

    @Before
    public void setup() throws IOException {
        Item item = new Item(objectMapper.readTree("{\"a\":\"b\"}"));
        Entry entry = new Entry(1, item.getSha256hex(), item.getBlobHash(), Instant.ofEpochSecond(1470403440), "b", EntryType.user);
        Record record = new Record(entry, item);

        ItemConverter itemConverter = mock(ItemConverter.class);
        Map<String, Field> fieldsByName = mock(Map.class);
        when(itemConverter.convertItem(item, fieldsByName)).thenReturn(ImmutableMap.of("a", new StringValue("b")));

        recordView = new RecordView(record, fieldsByName, itemConverter);
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
                "\"item\":[{\"a\":\"b\"}]" +
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
                "\"a\":\"b\"}]"));
    }
}
