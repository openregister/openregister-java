package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.resource.ResourceBase;

import java.io.IOException;
import java.util.List;

public class ListResultJsonSerializer extends JsonSerializer<ResourceBase.ListResultView> {
    @Override
    public void serialize(ResourceBase.ListResultView value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        List<Record> records = value.getRecords();
        JsonSerializer<Object> listSerializer = serializers.findValueSerializer(List.class);
        listSerializer.serialize(records, gen, serializers);
    }
}
