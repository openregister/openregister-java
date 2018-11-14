package uk.gov.register.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.register.core.Field;
import uk.gov.register.util.ResourceJsonFileReader;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class FieldsConfiguration {

    private final Collection<Field> fields;

    public FieldsConfiguration(String fieldsResourceJsonPath) {
        Collection<FieldConfigRecord> fieldConfigRecords = new ResourceJsonFileReader().readResourceFromPath(fieldsResourceJsonPath,
                new TypeReference<Map<String, FieldConfigRecord>>() {
        });
        fields = fieldConfigRecords.stream().map(FieldConfigRecord::getSingleItem).collect(toList());
    }

    public Optional<Field> getField(String fieldName) {
        return fields.stream().filter(f -> Objects.equals(f.fieldName, fieldName)).findFirst();
    }

    public Collection<Field> getAllFields() {
        return fields;
    }
}
