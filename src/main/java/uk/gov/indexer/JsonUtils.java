package uk.gov.indexer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.concurrent.Callable;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T fromJsonString(String content, Class<T> clazz) {
        return apply(() -> objectMapper.readValue(content, clazz));
    }

    public static <T> T fromStream(InputStream contentStream, Class<T> clazz) {
        return apply(() -> objectMapper.readValue(contentStream, clazz));
    }

    public static JsonNode fromBytesToJsonNode(byte[] bytes) {
        return apply(() -> objectMapper.readTree(bytes));
    }

    public static String toJsonString(Object object) {
        return apply(() -> objectMapper.writeValueAsString(object));
    }

    public static byte[] toBytes(Object object) {
        return apply(() -> objectMapper.writeValueAsBytes(object));
    }

    private static <T> T apply(Callable<T> function) {
        try {
            return function.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
