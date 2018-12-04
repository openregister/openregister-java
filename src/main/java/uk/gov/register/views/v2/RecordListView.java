package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.core.Field;
import uk.gov.register.core.Record;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.CsvRepresentationView;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecordListView implements CsvRepresentationView<ArrayNode> {
    private final Collection<Record> records;
    private final ItemConverter itemConverter;
    private final Map<String, Field> fieldsByName;
    private final List<String> fieldNames;
    private final ObjectMapper jsonObjectMapper = Jackson.newObjectMapper();

    public RecordListView(Collection<Record> records, final Map<String, Field> fieldsByName) {
        this.records = records;
        this.itemConverter = new ItemConverter();
        this.fieldsByName = fieldsByName;
        this.fieldNames = fieldsByName.values().stream().map(field -> field.fieldName).collect(Collectors.toList());
    }

    @JsonValue
    public List<JsonNode> getRecords() {
        return this.records.stream().map(r -> {
            ObjectNode record = (ObjectNode)(r.getItem().getContent());
            record.put("_id", r.getEntry().getKey());
            return record;
        }).collect(Collectors.toList());
    };

    public CsvSchema csvSchema() {
        CsvSchema.Builder schemaBuilder = new CsvSchema.Builder();
        schemaBuilder.addColumn("_id", CsvSchema.ColumnType.STRING);

        for (String value : this.fieldNames) {
            schemaBuilder.addColumn(value, CsvSchema.ColumnType.STRING);
        }

        return schemaBuilder.build();
    }

    protected ArrayNode getFlatRecordJson() {
        ArrayNode arrayNode = jsonObjectMapper.createArrayNode();
        getRecords().stream().forEach(arrayNode::add);
        return arrayNode;
    }

    @Override
    public CsvRepresentation<ArrayNode> csvRepresentation() {
        return new CsvRepresentation<>(csvSchema(), getFlatRecordJson());
    }
}
