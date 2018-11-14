package uk.gov.register.views.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Field;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterId;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.representations.CsvWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class RecordListViewTest {
    private Entry entry1;
    private Entry entry2;
    private Item item1;
    private Item item2;
    private Record record1;
    private Record record2;
    private Map<String, Field> fieldsByName;
    private JsonNode itemNode1;
    private JsonNode itemNode2;
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Before
    public void setup() throws IOException {
        Instant t1 = Instant.parse("2016-03-29T08:59:25Z");
        Instant t2 = Instant.parse("2016-03-28T09:49:26Z");

        this.entry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "ab"), t1, "123", EntryType.user);
        this.entry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "cd"), t2, "456", EntryType.user);

        itemNode1 = objectMapper.readTree("{\"address\":\"123\",\"street\":\"foo\"}");
        itemNode2 = objectMapper.readTree("{\"address\":\"456\",\"street\":\"bar\"}");

        this.item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "ab"), itemNode1);
        this.item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "cd"), itemNode2);

        this.record1 = new Record(entry1, item1);
        this.record2 = new Record(entry2, item2);

        this.fieldsByName = ImmutableMap.of(
                "street", new Field("street", "string", new RegisterId("foo"), Cardinality.ONE, "bla"),
                "address", new Field("address", "string", new RegisterId("foo"), Cardinality.ONE, "bla")
        );
    }

    @Test
    public void jsonRepresentation() throws IOException {
        RecordListView view = new RecordListView(Arrays.asList(this.record1, this.record2), fieldsByName);
        String result = objectMapper.writeValueAsString(view);
        JsonNode jsonNode = objectMapper.readTree(result);

        assertTrue(jsonNode.has("123"));
        assertTrue(jsonNode.has("456"));

        JsonNode record1Node = jsonNode.get("123");
        JsonNode record2Node = jsonNode.get("456");
        assertHasRecordFields(record1Node);
        assertHasRecordFields(record2Node);

        assertThat(record1Node.get("entry-number").intValue(), equalTo(1));
        assertThat(record2Node.get("entry-number").intValue(), equalTo(2));

        assertThat(record1Node.get("entry-timestamp").textValue(), equalTo("2016-03-29T08:59:25Z"));
        assertThat(record2Node.get("entry-timestamp").textValue(), equalTo("2016-03-28T09:49:26Z"));

        assertThat(record1Node.get("key").textValue(), equalTo("123"));
        assertThat(record2Node.get("key").textValue(), equalTo("456"));

        assertThat(record1Node.get("blob"), equalTo(itemNode1));
        assertThat(record2Node.get("blob"), equalTo(itemNode2));
    }

    @Test
    public void csvRepresentation() throws IOException {
        RecordListView view = new RecordListView(Arrays.asList(this.record1, this.record2), fieldsByName);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CsvWriter csvWriter = new CsvWriter();
        csvWriter.writeTo(view,
                RecordListView.class,
                null,
                null,
                null,
                null,
                outputStream);
        byte[] bytes = outputStream.toByteArray();
        String result = new String(bytes, StandardCharsets.UTF_8);

        assertThat(result, equalTo(
                "entry-number,entry-timestamp,key,street,address\r\n" +
                        "1,2016-03-29T08:59:25Z,123,foo,123\r\n" +
                        "2,2016-03-28T09:49:26Z,456,bar,456\r\n"
        ));
    }

    private void assertHasRecordFields(JsonNode node) {
        Set<String> fields = new HashSet<>();
        node.fieldNames().forEachRemaining(fields::add);
        assertThat(fields, equalTo(new HashSet<>(Arrays.asList("entry-number", "key", "entry-timestamp", "blob"))));
    }
}
