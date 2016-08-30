package uk.gov.register.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterDetail;

import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ArchiveCreatorTest {
    private static final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;

    private final Entry entry1 = new Entry(1, "entry1sha", Instant.parse("2016-07-24T16:55:00Z"));
    private final Entry entry2 = new Entry(2, "entry2sha", Instant.parse("2016-07-24T16:56:00Z"));

    private final Item entry1Item = new Item("entry1sha", jsonFactory.objectNode()
        .put("field-1", "entry1-field-1-value")
        .put("field-2", "entry1-field-2-value"));

    private final Item entry2Item = new Item("entry2sha", jsonFactory.objectNode()
        .put("field-1", "entry2-field-1-value")
        .put("field-2", "entry2-field-2-value"));

    @Test
    public void create_shouldCreateAnArchiveWithRegisterInfoEntriesAndItems() throws IOException {
        ArchiveCreator sutArchiveCreator= new ArchiveCreator();
        ArrayNode registerFields = jsonFactory.arrayNode()
            .add("field-1")
            .add("field-2");

        RegisterData registerData = new RegisterData(ImmutableMap.of(
            "register", jsonFactory.textNode("test-register"),
            "phase", jsonFactory.textNode("alpha"),
            "text", jsonFactory.textNode("test register"),
            "fields", registerFields
        ));

        RegisterDetail registerDetail = new RegisterDetail("test-domain", 2, 2, Optional.empty(), registerData);

        StreamingOutput streamingOutput = sutArchiveCreator.create(
            registerDetail,
            Arrays.asList(entry1, entry2),
            Arrays.asList(entry1Item, entry2Item));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        streamingOutput.write(baos);
        InputStream inputStreamFromArchive = new ByteArrayInputStream(baos.toByteArray());
        Map<String, JsonNode> archiveEntries = getArchiveEntries(inputStreamFromArchive);

        JsonNode actualRegisterNode = archiveEntries.get("register.json");
        assertThat(actualRegisterNode.get("domain").asText(), equalTo("test-domain"));
        assertThat(actualRegisterNode.get("total-records").asInt(), equalTo(2));
        assertThat(actualRegisterNode.get("total-entries").asInt(), equalTo(2));

        JsonNode actualRegisterRecord = actualRegisterNode.get("register-record");
        assertThat(actualRegisterRecord.get("register").asText(), equalTo("test-register"));
        assertThat(actualRegisterRecord.get("phase").asText(), equalTo("alpha"));
        assertThat(actualRegisterRecord.get("text").asText(), equalTo("test register"));
        assertThat(actualRegisterRecord.get("fields"), equalTo(registerFields));

        JsonNode actualEntry1Node = archiveEntries.get("entry/1.json");
        assertThat(actualEntry1Node.get("entry-number").asInt(), equalTo(1));
        assertThat(actualEntry1Node.get("entry-timestamp").asText(), equalTo("2016-07-24T16:55:00Z"));
        assertThat(actualEntry1Node.get("item-hash").asText(), equalTo("sha-256:entry1sha"));

        JsonNode actualEntry2Node = archiveEntries.get("entry/2.json");
        assertThat(actualEntry2Node.get("entry-number").asInt(), equalTo(2));
        assertThat(actualEntry2Node.get("entry-timestamp").asText(), equalTo("2016-07-24T16:56:00Z"));
        assertThat(actualEntry2Node.get("item-hash").asText(), equalTo("sha-256:entry2sha"));

        JsonNode actualEntry1ItemNode = archiveEntries.get("item/entry1sha.json");
        assertThat(actualEntry1ItemNode.get("field-1").asText(), equalTo("entry1-field-1-value"));
        assertThat(actualEntry1ItemNode.get("field-2").asText(), equalTo("entry1-field-2-value"));

        JsonNode actualEntry2ItemNode = archiveEntries.get("item/entry2sha.json");
        assertThat(actualEntry2ItemNode.get("field-1").asText(), equalTo("entry2-field-1-value"));
        assertThat(actualEntry2ItemNode.get("field-2").asText(), equalTo("entry2-field-2-value"));

        inputStreamFromArchive.close();
    }

    private Map<String, JsonNode> getArchiveEntries(InputStream inputStream) throws IOException {
        ZipInputStream zis = new ZipInputStream(inputStream);

        Map<String, JsonNode> entries = new HashMap<>();
        byte[] buffer = new byte[1024];
        int read = 0;
        for (ZipEntry entry; (entry = zis.getNextEntry()) != null; ) {
            StringBuilder sb = new StringBuilder();
            while ((read = zis.read(buffer, 0, 1024)) >= 0) {
                sb.append(new String(buffer, 0, read));
            }
            entries.put(entry.getName(), new ObjectMapper().readTree(sb.toString()));
        }

        return entries;
    }

}
