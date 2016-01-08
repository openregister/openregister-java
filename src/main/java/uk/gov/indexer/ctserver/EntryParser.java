package uk.gov.indexer.ctserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.dao.Entry;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class EntryParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntryParser.class);

    public Entry parse(MerkleTreeLeaf input, int overrideSerial) {
        byte[] rawdata = Base64.getDecoder().decode(input.getLeaf_input());
        byte[] rawtimestamp = Arrays.copyOfRange(rawdata, 2, 10);
        long timestamp = bytesToLong(rawtimestamp);
        LOGGER.info(String.format("Timestamp %d", timestamp));

        byte[] rawPayload = Arrays.copyOfRange(rawdata, 15, rawdata.length - 2);
        String payload = new String(rawPayload, StandardCharsets.UTF_8);
        LOGGER.info(String.format("Payload: '%s'", payload));

        String signature = createHash(input.getLeaf_input());

        try {
            ObjectMapper om = new ObjectMapper();
            JsonNode payloadNode = om.readTree(payload);

            // Need to convert to
            // { entry : <entry>, hash : <somehash> }
            ObjectNode object = JsonNodeFactory.instance.objectNode();
            object.put("hash", signature);
            object.set("entry", payloadNode);

            Object obj = om.treeToValue(object, Object.class);
            String tempObject = om.writeValueAsString(obj);
            LOGGER.info(tempObject);
            byte[] allData = tempObject.getBytes(StandardCharsets.UTF_8);
            return new Entry(overrideSerial, allData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip(); //need flip
        return buffer.getLong();
    }

    private String createHash(String input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(0x00);
            baos.write(Base64.getDecoder().decode(input));
            byte[] rawDataToSign = baos.toByteArray();

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(rawDataToSign);
            byte[] signature = md.digest();
            String encodedSignature = Base64.getEncoder().encodeToString(signature);
            return encodedSignature;

        } catch (Exception e) {
            LOGGER.info(e.toString());
        }
        return null;
    }
}
