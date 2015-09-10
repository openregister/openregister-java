package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.register.presentation.RecordView;
import uk.gov.register.presentation.view.ListResultView;

import java.io.IOException;
import java.util.List;

public class ListResultJsonSerializer extends JsonSerializer<ListResultView> {
    @Override
    public void serialize(ListResultView value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        List<RecordView> records = value.getRecords();
        JsonSerializer<Object> listSerializer = serializers.findValueSerializer(List.class);
        listSerializer.serialize(records, gen, serializers);
    }
}
