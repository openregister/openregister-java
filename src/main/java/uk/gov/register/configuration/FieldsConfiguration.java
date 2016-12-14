package uk.gov.register.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.register.core.Field;
import uk.gov.register.util.ResourceYamlFileReader;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FieldsConfiguration {

    private final Collection<Field> fields;

    public FieldsConfiguration(Optional<String> fieldsResourceYamlPath) {
        fields = new ResourceYamlFileReader().readResource(
                fieldsResourceYamlPath,
                "config/fields.yaml",
                new TypeReference<Map<String, Field>>() {
                });
    }

    public FieldsConfiguration(byte[] fieldsConfig) {
        fields = new ResourceYamlFileReader().readResource(
                fieldsConfig,
                new TypeReference<Map<String, Field>>() {
                });
    }

    public Field getField(String fieldName) {
        return fields.stream().filter(f -> Objects.equals(f.fieldName, fieldName)).findFirst().get();
    }
}
