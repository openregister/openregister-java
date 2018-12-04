package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Field;
import uk.gov.register.core.Record;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.CsvRepresentationView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RecordView implements CsvRepresentationView<ObjectNode> {
    private Iterable<Field> fields;
    private Record record;
    private final ItemView itemView;
    private final ObjectMapper jsonObjectMapper = Jackson.newObjectMapper();

    public RecordView(final Record record, final Map<String, Field> fieldsByName) {
        this(record, fieldsByName, new ItemConverter());
    }

    public RecordView(final Record record, final Map<String, Field> fieldsByName, ItemConverter itemConverter) throws FieldConversionException {
        this.fields = fields = fieldsByName.values();
        this.record = record;
        this.itemView = new ItemView(record.getItem(), fieldsByName, itemConverter);
    }

    private Record getRecord() {
        return record;
    }

    private Entry getEntry() {
        return getRecord().getEntry();
    }

    private Iterable<Field> getFields() {
        return this.fields;
    }

    private List<String> getFieldNames() {
        return StreamSupport.stream(getFields().spliterator(), false).map(field -> field.fieldName).collect(Collectors.toList());
    }

    @JsonValue
    public JsonNode getContent() {
        return getFlatRecordJson();
    }

    protected ObjectNode getFlatRecordJson() {
        ObjectNode result = jsonObjectMapper.convertValue(itemView, ObjectNode.class);
        result.put("_id", this.getEntry().getKey());
        return result;
    }

    public CsvSchema csvSchema() {
        CsvSchema.Builder schemaBuilder = new CsvSchema.Builder();
        schemaBuilder.addColumn("_id", CsvSchema.ColumnType.STRING);

        for (String value : getFieldNames()) {
            schemaBuilder.addColumn(value, CsvSchema.ColumnType.STRING);
        }

        return schemaBuilder.build();
    }

    @Override
    public CsvRepresentation<ObjectNode> csvRepresentation() {
        return new CsvRepresentation<>(csvSchema(), getFlatRecordJson());
    }
}
