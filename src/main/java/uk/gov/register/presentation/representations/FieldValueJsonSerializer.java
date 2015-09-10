package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.register.presentation.FieldValue;

import java.io.IOException;

public class FieldValueJsonSerializer extends JsonSerializer<FieldValue> {
    @Override
    public void serialize(FieldValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        JsonSerializer<Object> listSerializer = serializers.findValueSerializer(FieldValue.class);

        listSerializer.serialize(value, gen, serializers);
    }
}
