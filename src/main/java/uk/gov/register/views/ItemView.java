package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import net.logstash.logback.encoder.org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.*;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.representations.CsvRepresentation;
import uk.gov.register.views.representations.ExtraMediaType;
import uk.gov.register.views.representations.turtle.ItemTurtleWriter;

import javax.inject.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ItemView implements CsvRepresentationView<Map<String, FieldValue>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemView.class);

    private static final String END_OF_LINE = "\n";

    private final Iterable<Field> fields;
    private final Map<String, FieldValue> fieldValueMap;
    private final HashValue sha256hex;

    private final ObjectMapper jsonObjectMapper = Jackson.newObjectMapper();
    private final ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());

    public ItemView(final HashValue sha256hex, final Map<String, FieldValue> fieldValueMap, final Iterable<Field> fields) {
        this.fields = fields;
        this.fieldValueMap = fieldValueMap;
        this.sha256hex = sha256hex;
    }

    @JsonValue
    public Map<String, FieldValue> getContent() {
        return fieldValueMap;
    }

    @Override
    public CsvRepresentation<Map<String, FieldValue>> csvRepresentation() {
        final Iterable<String> fieldNames = StreamSupport.stream(fields.spliterator(), false)
                .map(f -> f.fieldName)
                .collect(Collectors.toList());

        return new CsvRepresentation<>(Item.csvSchema(fieldNames), fieldValueMap);
    }

    public String itemsTo(final String mediaType, final Provider<RegisterId> registerIdProvider, final RegisterResolver registerResolver) {
        final ByteArrayOutputStream outputStream;
        final ItemTurtleWriter entryTurtleWriter;
        String registerInTextFormatted = StringUtils.EMPTY;

        try {
            if (ExtraMediaType.TEXT_TTL_TYPE.getSubtype().equals(mediaType)) {
                outputStream = new ByteArrayOutputStream();
                entryTurtleWriter = new ItemTurtleWriter(registerIdProvider, registerResolver);

                entryTurtleWriter.writeTo(this, EntryListView.class, EntryListView.class, null, ExtraMediaType.TEXT_TTL_TYPE, null, outputStream);

                registerInTextFormatted = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            } else if (ExtraMediaType.TEXT_YAML_TYPE.getSubtype().equals(mediaType)) {
                registerInTextFormatted = yamlObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getContent());
            } else {
                registerInTextFormatted = jsonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getContent());
            }
        } catch (final IOException ex) {
            LOGGER.error("Error processing preview request. Impossible process the register items");
        }

        return StringEscapeUtils.escapeHtml(registerInTextFormatted.isEmpty() ? registerInTextFormatted : END_OF_LINE + registerInTextFormatted);
    }

    public HashValue getItemHash() {
        return sha256hex;
    }

    public Iterable<Field> getFields() {
        return fields;
    }
}
