package uk.gov.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Configuration {
    private final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private final Set<String> registers;
    private final Properties properties;
    private final Map<String, String> cloudSearchEndPoints;
    private final Map<String, String> cloudSearchWatermarkEndPoints;

    public Configuration(String[] args) {
        try {
            this.properties = new Properties();
            properties.load(configurationPropertiesStream(createConfigurationMap(args).get("config.file")));
            this.registers = extractConfiguredRegisters();
            this.cloudSearchEndPoints = createCloudSearchEndPoints(".cloudsearch.search.endpoint");
            this.cloudSearchWatermarkEndPoints = createCloudSearchEndPoints(".cloudsearch.highwatermark.endpoint");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> createCloudSearchEndPoints(String keyEndsWithString) {
        return properties.keySet()
                .stream()
                .map(Object::toString)
                .filter(key -> key.endsWith(keyEndsWithString))
                .collect(Collectors.toMap(key -> key.split("\\.")[0], properties::getProperty));
    }

    private InputStream configurationPropertiesStream(String fileName) throws IOException {
        if (fileName == null || fileName.trim().equals("")) {
            LOGGER.info("Configuration properties file not provided, using default application.properties file");
            return Application.class.getResourceAsStream("/application.properties");
        } else {
            LOGGER.info("Loading properties file: " + fileName);
            return new FileInputStream(new File(fileName));
        }
    }

    private Map<String, String> createConfigurationMap(String[] args) {
        Map<String, String> appParams = new HashMap<>();
        for (int i = 0; args != null && i < args.length; i++) {
            if (args[i].contains("=")) {
                String[] kv = args[i].split("=", 2);
                appParams.put(kv[0], kv[1]);
            }
        }
        return appParams;
    }

    private Set<String> extractConfiguredRegisters() {
        return properties.keySet()
                .stream()
                .filter(e -> ((String) e).endsWith(".postgres.db.connectionString"))
                .map(e -> ((String) e).split("\\.")[0])
                .collect(Collectors.toSet());
    }

    public Set<String> getRegisters() {
        return registers;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public Optional<String> cloudSearchEndPoint(String register) {
        return Optional.ofNullable(cloudSearchEndPoints.get(register));
    }

    public Optional<String> cloudSearchWaterMarkEndPoint(String register) {
        return Optional.ofNullable(cloudSearchWatermarkEndPoints.get(register));
    }

    public Optional<String> getCTServerEndpointForRegister(String register) {
        return Optional.ofNullable(properties.getProperty(register + ".ctserver"));
    }
}
