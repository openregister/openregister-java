package uk.gov.register.views.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.core.Cardinality;
import uk.gov.register.core.Field;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterId;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.representations.CsvWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BlobViewTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private JsonNode blobNode;
    private Item blob;
    private String blobHash;
    private BlobView view;

    @Before
    public void setup() throws IOException {
        blobNode = objectMapper.readTree("{\"address\":\"123\",\"street\":\"foo\"}");
        blob = new Item(blobNode);
        blobHash = blob.getBlobHash().encode();


        Map<String, Field> fieldsByName = ImmutableMap.of(
                "street", new Field("street", "string", new RegisterId("foo"), Cardinality.ONE, "bla"),
                "address", new Field("address", "string", new RegisterId("foo"), Cardinality.ONE, "bla")
        );

        this.view = new BlobView(blob, fieldsByName, new ItemConverter());
    }

    @Test
    public void jsonRepresentation() throws IOException {
        String result = objectMapper.writeValueAsString(view);

        JsonNode jsonNode = objectMapper.readTree(result);

        assertThat(jsonNode.get("_id").textValue(), equalTo(blobHash));

        ((ObjectNode) jsonNode).remove("_id");

        assertThat(jsonNode, equalTo(blobNode));
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
                        "\"" + blobHash + "\",foo,123\r\n"
        ));
    }

}
