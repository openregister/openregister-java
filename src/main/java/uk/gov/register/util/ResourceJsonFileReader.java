package uk.gov.register.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class ResourceJsonFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceJsonFileReader.class);

    private static final ObjectMapper JSON_OBJECT_MAPPER = Jackson.newObjectMapper();

    public <N> Collection<N> readResource(Optional<String> resourceJsonPath,
                                          String defaultResourceJsonFilePath,
                                          TypeReference<Map<String, N>> typeReference) {
        try {
            InputStream fieldsStream = getStreamFromUrl(resourceJsonPath, defaultResourceJsonFilePath);
            return JSON_OBJECT_MAPPER.<Map<String, N>>readValue(fieldsStream, typeReference).values();
        } catch (IOException e) {
            throw new UncheckedIOException("Error loading resources configuration file.", e);
        }
    }

    public <N> Collection<N> readResourceFromPath(String path, TypeReference<Map<String, N>> typeReference) {
        try {
            return JSON_OBJECT_MAPPER.<Map<String, N>>readValue(getStreamForFile(path), typeReference).values();
        } catch (IOException e) {
            throw new UncheckedIOException("Error loading resources configuration file.", e);
        }
    }

    private InputStream getStreamFromUrl(Optional<String> resourceJsonUrl, String defaultResourceJsonFilePath) {
        LOGGER.info("reading " + resourceJsonUrl.orElse(defaultResourceJsonFilePath));
        return resourceJsonUrl.map(this::getStreamForFile).orElse(getStreamForResource(defaultResourceJsonFilePath));
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
