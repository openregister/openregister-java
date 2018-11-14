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
import uk.gov.register.service.ItemConverter;
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

    public ItemView(Item item, final Map<String, Field> fieldsByName) {
        this(item, fieldsByName, new ItemConverter());
    }

    public ItemView(Item item, final Map<String, Field> fieldsByName, final ItemConverter itemConverter) {
        this.fields = fieldsByName.values();
        this.fieldValueMap = itemConverter.convertItem(item, fieldsByName);
        this.sha256hex = item.getSha256hex();
    }

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

    public HashValue getItemHash() {
        return sha256hex;
    }

    public Iterable<Field> getFields() {
        return fields;
    }
}
