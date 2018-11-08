package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.views.CsvRepresentationView;
import uk.gov.register.views.v2.EntryView;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class EntryListView implements CsvRepresentationView {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryListView.class);

    private static final String END_OF_LINE = "\n";

    private final Collection<Entry> entries;
    private final Optional<String> recordKey;

    private final ObjectMapper jsonObjectMapper = Jackson.newObjectMapper();

    public EntryListView(final Collection<Entry> entries) {
        this.entries = entries;
        recordKey = Optional.empty();
    }

    public EntryListView(final Collection<Entry> entries, final String recordKey) {
        this.entries = entries;
        this.recordKey = Optional.of(recordKey);
    }


    @JsonValue
    public Collection<EntryView> getEntries() {
        return entries.stream().map(entry -> new EntryView(entry)).collect(Collectors.toList());
    }

    @SuppressWarnings("unused, used from templates")
    public Optional<String> getRecordKey() {
        return recordKey;
    }

    @SuppressWarnings("unused, used from templates")
    public String getDownloadLink() {
        Optional<String> recordKey = getRecordKey();
        return recordKey.isPresent() ? String.format("record/%s/entries", recordKey.get()) : "entries";
    }

    @Override
    public CsvRepresentation<Collection<EntryView>> csvRepresentation() {
        return new CsvRepresentation<>(EntryView.csvSchema(), getEntries());
    }

    private ObjectNode getEntryJson(final Entry entry) {
        return jsonObjectMapper.convertValue(entry, ObjectNode.class);
    }
}
