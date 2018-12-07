package uk.gov.register.views;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Field;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterId;
import uk.gov.register.views.representations.CsvWriter;
import uk.gov.register.views.v2.BlobListView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BlobListViewTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private JsonNode blobNode1;
    private JsonNode blobNode2;
    private BlobListView view;
    private Item blob1;
    private Item blob2;
    private String blobHash1;
    private String blobHash2;

    @Before
    public void setup() throws IOException {
        blobNode1 = objectMapper.readTree("{\"address\":\"123\",\"street\":\"foo\"}");
        blob1 = new Item(blobNode1);
        blobHash1 = blob1.getBlobHash().encode();

        blobNode2 = objectMapper.readTree("{\"address\":\"456\",\"street\":\"bar\"}");
        blob2 = new Item(blobNode2);
        blobHash2 = blob2.getBlobHash().encode();

        Map<String, Field> fieldsByName = ImmutableMap.of(
                "street", new Field("street", "string", new RegisterId("foo"), Cardinality.ONE, "bla"),
                "address", new Field("address", "string", new RegisterId("foo"), Cardinality.ONE, "bla")
        );

        this.view = new BlobListView(ImmutableList.of(blob1, blob2), fieldsByName);
    }

    @Test
    public void jsonRepresentation() throws IOException {
        String result = objectMapper.writeValueAsString(view);

        JsonNode jsonNode = objectMapper.readTree(result);

        assertThat(jsonNode.size(), equalTo(2));
        JsonNode blob1 = jsonNode.get(0);
        JsonNode blob2 = jsonNode.get(1);

        assertThat(blob1.get("_id").textValue(), equalTo(blobHash1));
        assertThat(blob2.get("_id").textValue(), equalTo(blobHash2));

        ((ObjectNode) blob1).remove("_id");
        ((ObjectNode) blob2).remove("_id");

        assertThat(blob1, equalTo(blobNode1));
        assertThat(blob2, equalTo(blobNode2));
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
                "_id,street,address\r\n" +
                        "\"" + blobHash1 + "\",foo,123\r\n" +
                        "\"" + blobHash2 + "\",bar,456\r\n"
        ));
    }
}
