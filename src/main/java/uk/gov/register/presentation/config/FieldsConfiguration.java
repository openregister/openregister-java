package uk.gov.register.presentation.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FieldsConfiguration {

    private final List<Field> fields;

    public FieldsConfiguration(Optional<String> fieldsResourceYamlPath) {
        fields = new ResourceYamlFileReader().readResource(fieldsResourceYamlPath, "config/fields.yaml", new TypeReference<List<FieldData>>() {
        }, t -> t.entry);
    }

    public Field getField(String fieldName) {
        return fields.stream().filter(f -> Objects.equals(f.fieldName, fieldName)).findFirst().get();
    }

    @JsonIgnoreProperties({"hash", "last-updated", "serial-number"})
    private static class FieldData {
        @JsonProperty
        Field entry;
    }
}


