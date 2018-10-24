package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import net.logstash.logback.encoder.org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.views.representations.CsvRepresentation;
import uk.gov.register.views.representations.ExtraMediaType;
import uk.gov.register.views.representations.turtle.EntryListTurtleWriter;

import javax.inject.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EntryListView implements CsvRepresentationView {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryListView.class);

    private static final String END_OF_LINE = "\n";

    private final Collection<Entry> entries;
    private final Optional<String> recordKey;

    private final ObjectMapper jsonObjectMapper = Jackson.newObjectMapper();
    private final ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());

    public EntryListView(final Collection<Entry> entries) {
        this.entries = entries;
        recordKey = Optional.empty();
    }

    public EntryListView(final Collection<Entry> entries, final String recordKey) {
        this.entries = entries;
        this.recordKey = Optional.of(recordKey);
    }


    @JsonValue
    public Collection<Entry> getEntries() {
        return entries;
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
    public CsvRepresentation<Collection<Entry>> csvRepresentation() {
        return new CsvRepresentation<>(Entry.csvSchema(), getEntries());
    }

    private ObjectNode getEntryJson(final Entry entry) {
        return jsonObjectMapper.convertValue(entry, ObjectNode.class);
    }
}
