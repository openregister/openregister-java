package uk.gov.register.views;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Iterables;
import uk.gov.register.core.Field;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.Item;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.representations.CsvRepresentation;

import java.util.Map;

public class ItemView implements CsvRepresentationView<Map<String, FieldValue>> {
    private Iterable<Field> fields;
    private Map<String, FieldValue> fieldValueMap;
    private final HashValue sha256hex;

    public ItemView(HashValue sha256hex, Map<String, FieldValue> fieldValueMap, Iterable<Field> fields) {
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
        Iterable<String> fieldNames = Iterables.transform(fields, f -> f.getFieldName());
        return new CsvRepresentation<>(Item.csvSchema(fieldNames), fieldValueMap);
    }

    public HashValue getItemHash() {
        return sha256hex;
    }

    public Iterable<Field> getFields() {
        return fields;
    }

}
