package uk.gov.register.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonMapper {
    protected final ObjectMapper objectMapper;

    public JsonMapper() {
        objectMapper = new ObjectMapper();
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
            return objectMapper.writeValueAsBytes(obj);
        } catch (IOException e) {
            return ExceptionUtils.rethrow(e);
        }
    }
}