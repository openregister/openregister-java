package uk.gov.register.views.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Field;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterId;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RecordViewTest {
    private Entry entry1;
    private Item item1;
    private Record record1;
    private Map<String, Field> fieldsByName;
    private Map<String, FieldValue> itemValues;
    private Collection<Field> fields;
    private JsonNode itemNode1;
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Before
    public void setup() throws IOException {
        Instant t1 = Instant.parse("2016-03-29T08:59:25Z");
        itemNode1 = objectMapper.readTree("{\"address\":\"123\",\"street\":\"foo\"}");

        this.item1 = new Item(itemNode1);
        this.entry1 = new Entry(1, item1.getSha256hex(), item1.getBlobHash(), t1, "123", EntryType.user);
        this.record1 = new Record(entry1, item1);

        this.fieldsByName = ImmutableMap.of(
                "street", new Field("street", "string", new RegisterId("foo"), Cardinality.ONE, "bla"),
                "address", new Field("address", "string", new RegisterId("foo"), Cardinality.ONE, "bla")
        );

        ItemConverter itemConverter = new ItemConverter();

        this.itemValues = itemConverter.convertItem(this.item1, fieldsByName);
    }

    @Test
    public void jsonResponse() throws IOException {
        RecordView view = new RecordView(record1, fieldsByName);
        String result = objectMapper.writeValueAsString(view);
        JsonNode jsonNode = objectMapper.readTree(result);

        assertThat(jsonNode.get("entry-number").intValue(), equalTo(1));
        assertThat(jsonNode.get("entry-timestamp").textValue(), equalTo("2016-03-29T08:59:25Z"));
        assertThat(jsonNode.get("key").textValue(), equalTo("123"));
        assertThat(jsonNode.get("blob"), equalTo(this.itemNode1));
    }
}
