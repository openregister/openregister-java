package uk.gov.register.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class ResourceYamlFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceYamlFileReader.class);

    private static final ObjectMapper YAML_OBJECT_MAPPER = Jackson.newObjectMapper(new YAMLFactory());

    public <N> Collection<N> readResource(Optional<String> resourceYamlPath,
                                          String defaultResourceYamlFilePath,
                                          TypeReference<Map<String, N>> typeReference) {
        try {
            InputStream fieldsStream = getStreamFromUrl(resourceYamlPath, defaultResourceYamlFilePath);
            return YAML_OBJECT_MAPPER.<Map<String, N>>readValue(fieldsStream, typeReference).values();
        } catch (IOException e) {
            throw new UncheckedIOException("Error loading resources configuration file.", e);
        }
    }

    public <N> Collection<N> readResourceFromPath(String path, TypeReference<Map<String, N>> typeReference) {
        try {
            return YAML_OBJECT_MAPPER.<Map<String, N>>readValue(getStreamForFile(path), typeReference).values();
        } catch (IOException e) {
            throw new UncheckedIOException("Error loading resources configuration file.", e);
        }
    }

    private InputStream getStreamFromUrl(Optional<String> resourceYamlUrl, String defaultResourceYamlFilePath) {
        LOGGER.info("reading " + resourceYamlUrl.orElse(defaultResourceYamlFilePath));
        return resourceYamlUrl.map(this::getStreamForFile).orElse(getStreamForResource(defaultResourceYamlFilePath));
    }

    private InputStream getStreamForFile(String filePath) {
        try {
            return Files.newInputStream(Paths.get(filePath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private InputStream getStreamForResource(String path) {
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }


}
