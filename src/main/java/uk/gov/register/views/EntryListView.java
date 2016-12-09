package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.core.Entry;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Collection;
import java.util.Optional;

public class EntryListView implements CsvRepresentationView {
    private Collection<Entry> entries;
    private final Optional<String> recordKey;

    public EntryListView(Collection<Entry> entries) {
        this.entries = entries;
        this.recordKey = Optional.empty();
    }

    public EntryListView(Collection<Entry> entries, String recordKey) {
        this.entries = entries;
        this.recordKey = Optional.of(recordKey);
    }

    @JsonValue
    public Collection<Entry> getEntries() {
        return entries;
    }

    @SuppressWarnings("unused, used from templates")
    public Optional<String> getRecordKey() { return recordKey; }

    @Override
    public CsvRepresentation<Collection<Entry>> csvRepresentation() {
        return new CsvRepresentation<>(Entry.csvSchema(), getEntries());
    }
}
