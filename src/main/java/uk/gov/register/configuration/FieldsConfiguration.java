package uk.gov.register.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.register.core.Field;
import uk.gov.register.util.ResourceYamlFileReader;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class FieldsConfiguration {

    private final Collection<Field> fields;

    public FieldsConfiguration(String fieldsResourceYamlPath) {
        fields = new ResourceYamlFileReader().readResourceFromPath(fieldsResourceYamlPath, new TypeReference<Map<String, Field>>() {
        });
    }

    public Field getField(String fieldName) {
        return fields.stream().filter(f -> Objects.equals(f.getFieldName(), fieldName)).findFirst().get();
    }

    public Collection<Field> getAllFields() {
        return fields;
    }
}
