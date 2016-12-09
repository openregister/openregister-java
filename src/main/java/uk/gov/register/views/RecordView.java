package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.core.Entry;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.Record;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Map;
import java.util.Optional;

public class RecordView implements CsvRepresentationView {
    private final Entry entry;
    private final ItemView itemView;
    private final Iterable<String> fields;

    public RecordView(Entry entry, ItemView itemView, Iterable<String> fields) {
        this.entry = entry;
        this.itemView = itemView;
        this.fields = fields;
    }

    public String getPrimaryKey() {
        return entry.getKey();
    }

    @SuppressWarnings("unused, used to create the json representation of this class")
    @JsonValue
    public ObjectNode getRecordJson() {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectNode jsonNodes = objectMapper.convertValue(entry, ObjectNode.class);
        jsonNodes.remove("key");
        jsonNodes.setAll(objectMapper.convertValue(itemView, ObjectNode.class));
        return jsonNodes;
    }

    public Map<String, FieldValue> getContent() {
        return itemView.getContent();
    }

    @SuppressWarnings("unused, used from html templates")
    public Optional<FieldValue> getField(String fieldName) {
        return Optional.ofNullable(getContent().get(fieldName));
    }

    @Override
    public CsvRepresentation<ObjectNode> csvRepresentation() {
        return new CsvRepresentation<>(Record.csvSchema(fields), getRecordJson());
    }

    public ItemView getItemView() {
        return itemView;
    }

    public Entry getEntry() {
        return entry;
    }
}
