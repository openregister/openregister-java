package uk.gov.register.presentation.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonObjectMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T convert(Object object, TypeReference<T> typeReference) {
        try {
            String s = objectMapper.writeValueAsString(object);
            return objectMapper.readValue(s, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
