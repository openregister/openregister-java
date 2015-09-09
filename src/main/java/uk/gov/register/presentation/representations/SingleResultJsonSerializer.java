package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.view.SingleResultView;

import java.io.IOException;

public class SingleResultJsonSerializer extends JsonSerializer<SingleResultView> {
    @Override
    public void serialize(SingleResultView value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Record record = value.getRecord();
        JsonSerializer<Object> listSerializer = serializers.findValueSerializer(Record.class);
        listSerializer.serialize(record, gen, serializers);
    }
}
