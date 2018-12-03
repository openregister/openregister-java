package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.core.Field;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.Item;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ItemView implements CsvRepresentationView<Map<String, FieldValue>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemView.class);

    private static final String END_OF_LINE = "\n";

    private final Iterable<Field> fields;
    private final Map<String, FieldValue> fieldValueMap;
    private final HashValue sha256hex;

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
