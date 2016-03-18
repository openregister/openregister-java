package uk.gov.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class FieldsConfiguration {

    private final List<Field> fields;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public FieldsConfiguration(Optional<String> fieldsResourceYamlPath) {
        try {
            InputStream fieldsStream = getFieldsStream(fieldsResourceYamlPath);
            ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());
            List<FieldData> rawFields = yamlObjectMapper.readValue(fieldsStream, new TypeReference<List<FieldData>>() {
            });
            fields = Lists.transform(rawFields, m -> m.entry);
        } catch (IOException e) {
            throw new RuntimeException("Error loading fields configuration.", e);
        }
    }

    protected InputStream getFieldsStream(Optional<String> fieldsResourceYamlPath) throws FileNotFoundException {
        if (fieldsResourceYamlPath.isPresent()) {
            logger.info("Loading external file '" + fieldsResourceYamlPath.get() + "' for fields data.");
            return new FileInputStream(new File(fieldsResourceYamlPath.get()));
        } else {
            logger.info("Loading internal file 'config/fields.yaml' for fields data.");
            return this.getClass().getClassLoader().getResourceAsStream("config/fields.yaml");
        }
    }

    public Field getField(String fieldName) {
        return fields.stream().filter(f -> Objects.equals(f.fieldName, fieldName)).findFirst().get();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FieldData {
        @JsonProperty
        Field entry;
    }
}
