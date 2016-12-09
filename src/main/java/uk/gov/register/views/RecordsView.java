package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.core.Record;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecordsView implements CsvRepresentationView {
    private List<RecordView> records;
    private Iterable<String> fields;

    public RecordsView(List<RecordView> records, Iterable<String> fields) {
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
        return new CsvRepresentation<>(Record.csvSchema(fields), getRecords());
    }
}
