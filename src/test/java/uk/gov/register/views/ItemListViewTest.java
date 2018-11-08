package uk.gov.register.views;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Field;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterId;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.representations.CsvWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ItemListViewTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private JsonNode itemNode1;
    private JsonNode itemNode2;
    private ItemListView view;

    @Before
    public void setup() throws IOException {
        itemNode1 = objectMapper.readTree("{\"address\":\"123\",\"street\":\"foo\"}");
        Item item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "ab"), itemNode1);

        itemNode2 = objectMapper.readTree("{\"address\":\"456\",\"street\":\"bar\"}");
        Item item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "cd"), itemNode2);

        Map<String, Field> fieldsByName = ImmutableMap.of(
                "street", new Field("street", "string", new RegisterId("foo"), Cardinality.ONE, "bla"),
                "address", new Field("address", "string", new RegisterId("foo"), Cardinality.ONE, "bla")
        );

        this.view = new ItemListView(ImmutableList.of(item1, item2), fieldsByName);
    }

    @Test
    public void jsonRepresentation() throws IOException {
        String result = objectMapper.writeValueAsString(view);

        JsonNode jsonNode = objectMapper.readTree(result);

        assertTrue(jsonNode.has("sha-256:ab"));
        assertTrue(jsonNode.has("sha-256:cd"));

        assertThat(jsonNode.get("sha-256:ab"), equalTo(itemNode1));
        assertThat(jsonNode.get("sha-256:cd"), equalTo(itemNode2));
    }

    @Test
    public void csvRepresentation() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CsvWriter csvWriter = new CsvWriter();
        csvWriter.writeTo(view,
                ArrayNode.class,
                null,
                null,
                null,
                null,
                outputStream);
        byte[] bytes = outputStream.toByteArray();
        String result = new String(bytes, StandardCharsets.UTF_8);

        assertThat(result, equalTo(
                "blob-hash,street,address\r\n" +
                        "sha-256:cd,bar,456\r\n" +
                        "sha-256:ab,foo,123\r\n"

        ));
    }
}
