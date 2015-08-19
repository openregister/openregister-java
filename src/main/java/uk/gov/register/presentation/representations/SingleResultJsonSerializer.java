package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.resource.ResourceBase;

import java.io.IOException;

public class SingleResultJsonSerializer extends JsonSerializer<ResourceBase.SingleResultView> {
    @Override
    public void serialize(ResourceBase.SingleResultView value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Record record = value.getRecord();
        JsonSerializer<Object> listSerializer = serializers.findValueSerializer(Record.class);
        listSerializer.serialize(record, gen, serializers);
    }
}
