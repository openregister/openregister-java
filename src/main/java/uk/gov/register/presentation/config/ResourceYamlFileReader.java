package uk.gov.register.presentation.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class ResourceYamlFileReader {
    private final Logger logger = LoggerFactory.getLogger(ResourceYamlFileReader.class);
    private final ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());

    @FunctionalInterface
    interface RawDataConverter<R, S> {
        S convert(R t);
    }

    public <M, T> List<T> readResource(Optional<String> resourceYamlPath,
                                              String defaultResourceYamlFilePath,
                                              TypeReference<List<M>> typeReference,
                                              RawDataConverter<M, T> rawDataConverter) {
        try {
            InputStream fieldsStream = new ResourceYamlFileReader().getStreamFromFile(resourceYamlPath, defaultResourceYamlFilePath);
            return yamlObjectMapper.<List<M>>readValue(fieldsStream, typeReference).stream().map(rawDataConverter::convert).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error loading resource configuration file.", e);
        }
    }

    private InputStream getStreamFromFile(Optional<String> resourceYamlPath, String defaultResourceYamlFilePath) throws FileNotFoundException {
        if (resourceYamlPath.isPresent()) {
            logger.info("Loading external file '" + resourceYamlPath.get() + ".");
            return new FileInputStream(new File(resourceYamlPath.get()));
        } else {
            logger.info("Loading internal file '" + defaultResourceYamlFilePath + "'.");
            return this.getClass().getClassLoader().getResourceAsStream(defaultResourceYamlFilePath);
        }
    }
}
