package uk.gov.register.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ToArrayConverter extends JsonSerializer<Object> {
    public ToArrayConverter() {
        super();
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray(1);
        gen.writeString(value.toString());
        gen.writeEndArray();
    }
}
