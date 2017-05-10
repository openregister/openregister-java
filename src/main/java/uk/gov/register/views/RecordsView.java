package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterables;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.core.Field;
import uk.gov.register.core.Record;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordsView implements CsvRepresentationView {
    private List<RecordView> records;
    private final boolean isRegister;

    private Iterable<Field> fields;

    public RecordsView(List<RecordView> records, Iterable<Field> fields, boolean isRegister) {
        this.records = records;
        this.fields = fields;
        this.isRegister = isRegister;
    }

    public RecordsView(List<RecordView> records, Iterable<Field> fields) {
        this(records, fields, true);
    }

    @JsonValue
    public Map<String, JsonNode> recordsJson() {
        Map<String, JsonNode> records = new HashMap<>();
        getRecords().forEach(recordView -> records.putAll(recordView.getRecordJson()));
        return records;
    }

    ArrayNode getFlatRecordsJson() {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        records.forEach( rv -> arrayNode.addAll( rv.getFlatRecordJson() ));
        return arrayNode;
    }

    public List<RecordView> getRecords() {
        return records;
    }

    @Override
    public CsvRepresentation<ArrayNode> csvRepresentation() {
        Iterable<String> fieldNames = Iterables.transform(fields, f -> f.fieldName);
        return new CsvRepresentation<>(Record.csvSchema(fieldNames), getFlatRecordsJson());
    }

    public Iterable<Field> getFields() {
        return fields;
    }

    @SuppressWarnings("unused, used by template")
    public boolean displayEntryKey() {
        return !isRegister;
    }

    @SuppressWarnings("unused, used by template")
    public boolean resolveLinks() { return false; }
}
