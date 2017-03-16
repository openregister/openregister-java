package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterables;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.core.Field;
import uk.gov.register.core.Record;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecordsView implements CsvRepresentationView {
    private List<RecordView> records;


    private Iterable<Field> fields;

    public RecordsView(List<RecordView> records, Iterable<Field> fields) {
        this.records = records;
        this.fields = fields;
    }

    @JsonValue
    public Map<String, RecordView> recordsJson() {
        return getRecords().stream().collect(Collectors.toMap(RecordView::getPrimaryKey, r -> r));
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
}
