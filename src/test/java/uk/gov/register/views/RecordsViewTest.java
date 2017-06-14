package uk.gov.register.views;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.register.core.*;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecordsViewTest {
    @Test
    public void recordsJson_returnsTheMapOfRecords() throws IOException, JSONException {
        ObjectMapper objectMapper = Jackson.newObjectMapper();

        Instant t1 = Instant.parse("2016-03-29T08:59:25Z");
        Instant t2 = Instant.parse("2016-03-28T09:49:26Z");

        Entry entry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), t1, "123", EntryType.user);
        Entry entry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "cd"), t2, "456", EntryType.user);
        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "ab"), objectMapper.readTree("{\"address\":\"123\",\"street\":\"foo\"}"));
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "cd"), objectMapper.readTree("{\"address\":\"456\",\"street\":\"bar\"}"));
        Record record1 = new Record(entry1, Arrays.asList(item1));
        Record record2 = new Record(entry2, Arrays.asList(item2));

        ItemConverter itemConverter = mock(ItemConverter.class);
        Map<String, Field> fieldsByName = mock(Map.class);
        when(itemConverter.convertItem(item1, fieldsByName)).thenReturn(ImmutableMap.of("address", new StringValue("123"),
                "street", new StringValue("foo")));
        when(itemConverter.convertItem(item2, fieldsByName )).thenReturn(ImmutableMap.of("address", new StringValue("456"),
                "street", new StringValue("bar")));

        RecordsView recordsView = new RecordsView(Arrays.asList(record1, record2), fieldsByName, itemConverter, false, false);

        Map<String, JsonNode> result = recordsView.getNestedRecordJson();
        assertThat(result.size(), equalTo(2));

        JSONAssert.assertEquals(
                "{" +
                        "\"index-entry-number\":\"1\"," +
                        "\"entry-number\":\"1\"," +
                        "\"entry-timestamp\":\"2016-03-29T08:59:25Z\"," +
                        "\"key\":\"123\"," +
                        "\"item\":[{" +
                        "\"address\":\"123\"," +
                        "\"street\":\"foo\"" +
                        "}]}",
                Jackson.newObjectMapper().writeValueAsString(result.get("123")),
                false
        );

        JSONAssert.assertEquals(
                "{" +
                        "\"index-entry-number\":\"2\"," +
                        "\"entry-number\":\"2\"," +
                        "\"entry-timestamp\":\"2016-03-28T09:49:26Z\"," +
                        "\"key\":\"456\"," +
                        "\"item\":[{" +
                        "\"address\":\"456\"," +
                        "\"street\":\"bar\"" +
                        "}]}",
                Jackson.newObjectMapper().writeValueAsString(result.get("456")),
                false
        );
    }
}
