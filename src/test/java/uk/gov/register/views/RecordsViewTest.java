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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecordsViewTest {
    @Test
    public void recordsJson_returnsTheMapOfRecords_forUserEntries() throws IOException, JSONException {
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
        Map<String, Field> registerFieldsByName = mock(Map.class);
        Map<String, Field> metadataFieldsByName = mock(Map.class);
        when(itemConverter.convertItem(item1, registerFieldsByName)).thenReturn(ImmutableMap.of("address", new StringValue("123"),
                "street", new StringValue("foo")));
        when(itemConverter.convertItem(item2, registerFieldsByName )).thenReturn(ImmutableMap.of("address", new StringValue("456"),
                "street", new StringValue("bar")));

        RecordsView recordsView = new RecordsView(Arrays.asList(record1, record2), registerFieldsByName, metadataFieldsByName, itemConverter, false, false);

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
    
    @Test
    public void recordsJson_returnsTheMapOfRecords_forSystemEntries() throws IOException, JSONException {
        ObjectMapper objectMapper = Jackson.newObjectMapper();

        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "item1"), objectMapper.readTree("{\"name\":\"school\"}"));
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "item2"), objectMapper.readTree("{\"custodian\":\"Joe Bloggs\"}"));
        Item item3 = new Item(new HashValue(HashingAlgorithm.SHA256, "item3"), objectMapper.readTree("{\"field\":\"school\",\"text\":\"A school in the UK.\"}"));
        Item item4 = new Item(new HashValue(HashingAlgorithm.SHA256, "item4"), objectMapper.readTree("{\"fields\":[\"school\"],\"register\":\"school\",\"text\":\"Schools in the UK.\"}"));
        Entry entry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "item1"), Instant.parse("2016-03-29T08:59:25Z"), "name", EntryType.system);
        Entry entry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "item2"), Instant.parse("2016-03-28T09:49:26Z"), "custodian", EntryType.system);
        Entry entry3 = new Entry(3, new HashValue(HashingAlgorithm.SHA256, "item3"), Instant.parse("2016-03-28T09:49:26Z"), "field:school", EntryType.system);
        Entry entry4 = new Entry(4, new HashValue(HashingAlgorithm.SHA256, "item4"), Instant.parse("2016-03-28T09:49:26Z"), "register:school", EntryType.system);
        Record record1 = new Record(entry1, Arrays.asList(item1));
        Record record2 = new Record(entry2, Arrays.asList(item2));
        Record record3 = new Record(entry3, Arrays.asList(item3));
        Record record4 = new Record(entry4, Arrays.asList(item4));

        Field fieldField = new Field("field", "string", new RegisterName("field"), Cardinality.ONE, "");
        Field fieldsField = new Field("fields", "string", new RegisterName("fields"), Cardinality.MANY, "");
        Field registerField = new Field("register", "string", new RegisterName("register"), Cardinality.ONE, "");
        Field textField = new Field("text", "string", null, Cardinality.ONE, "");

        ItemConverter itemConverter = mock(ItemConverter.class);
        Map<String, Field> registerFieldsByName = mock(Map.class);
        Map<String, Field> metadataFieldsByName = mock(Map.class);

        when(itemConverter.convertItem(item1, metadataFieldsByName)).thenReturn(ImmutableMap.of("name", new StringValue("school")));
        when(itemConverter.convertItem(item2, metadataFieldsByName)).thenReturn(ImmutableMap.of("custodian", new StringValue("Joe Bloggs")));
        when(itemConverter.convertItem(item3, metadataFieldsByName)).thenReturn(ImmutableMap.of("field:school", new StringValue("{\"field\":\"school\",\"text\":\"A school in the UK.\"}")));
        when(itemConverter.convertItem(item4, metadataFieldsByName)).thenReturn(ImmutableMap.of("register:school", new StringValue("{\"fields\":[\"school\"],\"register\":\"school\",\"text\":\"Schools in the UK.\"}")));
        
        RecordsView recordsView = new RecordsView(Arrays.asList(record1, record2, record3, record4), registerFieldsByName, metadataFieldsByName, itemConverter, false, false);

        Map<String, JsonNode> result = recordsView.getNestedRecordJson();
        assertThat(result.size(), equalTo(4));

        JSONAssert.assertEquals(
                "{\"index-entry-number\":\"1\",\"entry-number\":\"1\",\"entry-timestamp\":\"2016-03-29T08:59:25Z\",\"key\":\"name\",\"item\":[{\"name\":\"school\"}]}",
                Jackson.newObjectMapper().writeValueAsString(result.get("name")),
                false
        );

        JSONAssert.assertEquals(
                "{\"index-entry-number\":\"2\",\"entry-number\":\"2\",\"entry-timestamp\":\"2016-03-28T09:49:26Z\",\"key\":\"custodian\",\"item\":[{\"custodian\":\"Joe Bloggs\"}]}",
                Jackson.newObjectMapper().writeValueAsString(result.get("custodian")),
                false
        );

        JSONAssert.assertEquals(
                "\"{\"index-entry-number\":\"3\",\"entry-number\":\"3\",\"entry-timestamp\":\"2016-03-28T09:49:26Z\",\"key\":\"field:school\",\"item\":[{\"field:school\":\"{\"field\":\"school\",\"text\":\"A school in the UK.\"}\"}]}\"",
                Jackson.newObjectMapper().writeValueAsString(result.get("field:school")),
                false
        );

        JSONAssert.assertEquals(
                "{\"index-entry-number\":\"4\",\"entry-number\":\"4\",\"entry-timestamp\":\"2016-03-28T09:49:26Z\",\"key\":\"register:school\",\"item\":[{\"register:school\":\"{\\\"fields\\\":[\\\"school\\\"],\\\"register\\\":\\\"school\\\",\\\"text\\\":\\\"Schools in the UK.\\\"}\"}]}",
                Jackson.newObjectMapper().writeValueAsString(result.get("register:school")),
                false
        );
    }
}
