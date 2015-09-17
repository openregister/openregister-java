package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.view.SingleEntryView;

import java.io.IOException;

public class SingleEntryJsonSerializer extends JsonSerializer<SingleEntryView> {
    @Override
    public void serialize(SingleEntryView value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        EntryView entry = value.getEntry();
        JsonSerializer<Object> listSerializer = serializers.findValueSerializer(EntryView.class);
        listSerializer.serialize(entry, gen, serializers);
    }
}
