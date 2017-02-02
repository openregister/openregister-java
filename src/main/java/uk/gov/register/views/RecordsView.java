package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Iterables;
import uk.gov.register.core.Field;
import uk.gov.register.core.Record;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Collection;
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

    public List<RecordView> getRecords() {
        return records;
    }

    @Override
    public CsvRepresentation<Collection<RecordView>> csvRepresentation() {
        Iterable<String> fieldNames = Iterables.transform(fields, f -> f.fieldName);
        return new CsvRepresentation<>(Record.csvSchema(fieldNames), getRecords());
    }

    public Iterable<Field> getFields() {
        return fields;
    }
}
