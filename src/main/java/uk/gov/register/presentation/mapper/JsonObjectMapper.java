package uk.gov.register.presentation.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonObjectMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T convert(Object object, Class<T> klass) {
        try {
            String s = objectMapper.writeValueAsString(object);
            return objectMapper.readValue(s, klass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode convertToJsonNode(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
