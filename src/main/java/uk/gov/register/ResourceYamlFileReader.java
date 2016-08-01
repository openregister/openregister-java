package uk.gov.register;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class ResourceYamlFileReader {
    private final Logger logger = LoggerFactory.getLogger(ResourceYamlFileReader.class);
    private final ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());

    public <N> Collection<N> readResource(Optional<String> resourceYamlPath,
                                          String defaultResourceYamlFilePath,
                                          TypeReference<Map<String, N>> typeReference) {
        try {
            InputStream fieldsStream = new ResourceYamlFileReader().getStreamFromFile(resourceYamlPath, defaultResourceYamlFilePath);
            return yamlObjectMapper.<Map<String, N>>readValue(fieldsStream, typeReference).values();
        } catch (IOException e) {
            throw new RuntimeException("Error loading resources configuration file.", e);
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
