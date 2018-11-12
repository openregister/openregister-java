package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.core.Entry;
import uk.gov.register.views.CsvRepresentationView;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Collection;
import java.util.stream.Collectors;

public class EntryListView implements CsvRepresentationView {
    private final Collection<Entry> entries;

    public EntryListView(final Collection<Entry> entries) {
        this.entries = entries;
    }

    @JsonValue
    public Collection<EntryView> getEntries() {
        return entries.stream().map(entry -> new EntryView(entry)).collect(Collectors.toList());
    }

    @Override
    public CsvRepresentation<Collection<EntryView>> csvRepresentation() {
        return new CsvRepresentation<>(EntryView.csvSchema(), getEntries());
    }
}
