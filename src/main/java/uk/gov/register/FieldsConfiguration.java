package uk.gov.register;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;

public class FieldsConfiguration {

    private final Collection<Field> fields;

    public FieldsConfiguration(Optional<String> fieldsResourceYamlPath) {
        fields = new ResourceYamlFileReader().readResource(
                fieldsResourceYamlPath,
                "config/fields.yaml",
                new TypeReference<Map<String, Field>>() {
                });
    }

    public Field getField(String fieldName) {
        return fields.stream().filter(f -> Objects.equals(f.fieldName, fieldName)).findFirst().get();
    }
}
