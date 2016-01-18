package uk.gov.indexer.ctserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Hex;
import uk.gov.indexer.dao.Entry;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class EntryParser {
    public Entry parse(MerkleTreeLeaf input, int overrideSerial) {
        byte[] rawdata = Base64.getDecoder().decode(input.leaf_input);
        byte[] rawPayload = Arrays.copyOfRange(rawdata, 15, rawdata.length - 2);
        String payload = new String(rawPayload, StandardCharsets.UTF_8);

        try {
            ObjectMapper om = new ObjectMapper();
            JsonNode payloadNode = om.readTree(payload);

            // Need to convert to
            // { entry : <json_data>, hash : <somehash> }
            ObjectNode object = JsonNodeFactory.instance.objectNode();
            object.put("hash", createItemHash(rawPayload));
            object.set("entry", payloadNode);

            Object obj = om.treeToValue(object, Object.class);
            String tempObject = om.writeValueAsString(obj);
            byte[] allData = tempObject.getBytes(StandardCharsets.UTF_8);
            return new Entry(overrideSerial, allData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createItemHash(byte[] payload) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(payload);
            byte[] signature = md.digest();
            return Hex.encodeHexString(signature);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
