package uk.gov;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

class Configuration {
    private final Set<String> registers;
    private final Properties properties;

    public Configuration(String[] args) {
        try {
            this.properties = new Properties();
            properties.load(configurationPropertiesStream(createConfigurationMap(args).get("config.file")));
            this.registers = extractConfiguredRegisters();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private InputStream configurationPropertiesStream(String fileName) throws IOException {
        if (fileName == null || fileName.trim().equals("")) {
            ConsoleLogger.log("Configuration properties file not provided, using default application.properties file");
            return Application.class.getResourceAsStream("/application.properties");
        } else {
            ConsoleLogger.log("Loading properties file: " + fileName);
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

}
