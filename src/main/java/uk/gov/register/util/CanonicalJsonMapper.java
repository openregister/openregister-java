package uk.gov.register.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public class CanonicalJsonMapper {
    private final ObjectMapper objectMapper;

    public CanonicalJsonMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
    }

    public JsonNode readFromBytes(byte[] body) {
        try {
            return objectMapper.readValue(body, JsonNode.class);
        } catch (IOException e) {
            return ExceptionUtils.rethrow(e);
        }
    }

    public byte[] writeToBytes(JsonNode jsonNode) {
        try {
            // Method from http://stackoverflow.com/questions/18952571/jackson-jsonnode-to-string-with-sorted-keys
            Object obj = objectMapper.treeToValue(jsonNode, Object.class);
            // for some reason, writeValueAsString(obj).getBytes() doesn't re-escape unicode, but writeValueAsBytes does
            // our canonical form requires raw unescaped unicode, so we need this version
            return objectMapper.writeValueAsString(obj).getBytes();
        } catch (IOException e) {
            return ExceptionUtils.rethrow(e);
        }
    }
}
