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

        itemNode1 = objectMapper.readTree("{\"address\":\"123\",\"street\":\"foo\"}");
        itemNode2 = objectMapper.readTree("{\"address\":\"456\",\"street\":\"bar\"}");

        this.item1 = new Item(itemNode1);
        this.item2 = new Item(itemNode2);

        this.entry1 = new Entry(1, item1.getSha256hex(), item1.getBlobHash(), t1, "123", EntryType.user);
        this.entry2 = new Entry(2, item2.getSha256hex(), item2.getBlobHash(), t2, "456", EntryType.user);

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
        assertTrue(jsonNode.isArray());
        JsonNode record1Node = jsonNode.get(0);
        JsonNode record2Node = jsonNode.get(1);
        assertHasRecordFields(record1Node);
        assertHasRecordFields(record2Node);

        assertThat(record1Node.get("_id").asInt(), equalTo(123));
        assertThat(record1Node.get("street").asText(), equalTo("foo"));
        assertThat(record1Node.get("address").asInt(), equalTo(123));
        assertThat(record2Node.get("_id").asInt(), equalTo(456));
        assertThat(record2Node.get("street").asText(), equalTo("bar"));
        assertThat(record2Node.get("address").asInt(), equalTo(456));
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
                "_id,street,address\r\n123,foo,123\r\n456,bar,456\r\n"
        ));
    }

    private void assertHasRecordFields(JsonNode node) {
        Set<String> fields = new HashSet<>();
        node.fieldNames().forEachRemaining(fields::add);
        assertThat(fields, equalTo(new HashSet<>(Arrays.asList("address", "street", "_id"))));
    }
}
