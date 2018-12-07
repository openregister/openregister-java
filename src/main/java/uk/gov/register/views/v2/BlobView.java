package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.core.Field;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.Item;
import uk.gov.register.core.StringValue;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.CsvRepresentationView;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BlobView implements CsvRepresentationView<Map<String, FieldValue>> {
    private final Iterable<Field> fields;
    private final Map<String, FieldValue> fieldValueMap;

    public BlobView(Item item, final Map<String, Field> fieldsByName, final ItemConverter itemConverter) {
        this.fields = fieldsByName.values();
        this.fieldValueMap = new HashMap(itemConverter.convertItem(item, fieldsByName));
        this.fieldValueMap.put("_id", new StringValue(item.getBlobHash().encode()));
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

    public Iterable<Field> getFields() {
        return fields;
    }
}
