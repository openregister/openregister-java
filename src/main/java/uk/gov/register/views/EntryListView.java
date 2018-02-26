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
import uk.gov.register.core.RegisterName;
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

    public String entriesTo(final String mediaType, final Provider<RegisterName> registerNameProvider, final RegisterResolver registerResolver) {
        final ByteArrayOutputStream outputStream;
        final EntryListTurtleWriter entryTurtleWriter;
        String registerInTextFormatted = StringUtils.EMPTY;

        try {
            if (ExtraMediaType.TEXT_TTL_TYPE.getSubtype().equals(mediaType)) {
                outputStream = new ByteArrayOutputStream();
                entryTurtleWriter = new EntryListTurtleWriter(registerNameProvider, registerResolver);

                entryTurtleWriter.writeTo(this, EntryListView.class, EntryListView.class, null, ExtraMediaType.TEXT_TTL_TYPE, null, outputStream);

                registerInTextFormatted = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            } else if (ExtraMediaType.TEXT_YAML_TYPE.getSubtype().equals(mediaType)) {
                registerInTextFormatted = yamlObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getNestedEntryJson());
            } else {
                registerInTextFormatted = jsonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getNestedEntryJson());
            }
        } catch (final IOException ex) {
            LOGGER.error("Error processing preview request. Impossible process the register items");
        }

        return StringEscapeUtils.escapeHtml(registerInTextFormatted.isEmpty() ? registerInTextFormatted : END_OF_LINE + registerInTextFormatted);
    }

    @SuppressWarnings("unused, used by JSON renderer")
    public Map<String, JsonNode> getNestedEntryJson() {
        final Map<String, JsonNode> entryMap = new HashMap<>();

        entries.forEach(entry -> {
            final ObjectNode jsonNode = getEntryJson(entry);
            entryMap.put(entry.getKey(), jsonNode);
        });

        return entryMap;
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
