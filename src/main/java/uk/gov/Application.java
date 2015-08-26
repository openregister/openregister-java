package uk.gov;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Application {

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        Map<String, String> propertiesMap = createConfigurationMap(args);

        Properties properties = new Properties();
        properties.load(configurationPropertiesStream(propertiesMap.get("config.file")));

        Set<String> configuredRegisters = properties.keySet()
                .stream()
                .filter(e -> ((String) e).endsWith(".postgres.db.connectionString"))
                .map(e -> ((String) e).split("\\.")[0])
                .collect(Collectors.toSet());

        ExecutorService executorService = Executors.newFixedThreadPool(configuredRegisters.size());
        try {
            for (String configuredRegister : configuredRegisters) {
                SourcePostgresDB sourceDB = new SourcePostgresDB(properties.getProperty(configuredRegister + ".source.postgres.db.connectionString"));
                DestinationPostgresDB destinationDB = new DestinationPostgresDB(properties.getProperty(configuredRegister + ".destination.postgres.db.connectionString"));
                IndexerTask indexer = new IndexerTask(configuredRegister, sourceDB, destinationDB);
                executorService.submit(indexer);
            }

            Thread.currentThread().join();
        } finally {
            executorService.shutdown();
        }
    }

    private static InputStream configurationPropertiesStream(String fileName) throws IOException {
        if (fileName == null || fileName.trim().equals("")) {
            ConsoleLogger.log("Configuration properties file not provided, using default application.properties file");
            return Application.class.getResourceAsStream("/application.properties");
        } else {
            ConsoleLogger.log("Loading properties file: " + fileName);
            return new FileInputStream(new File(fileName));
        }
    }

    private static Map<String, String> createConfigurationMap(String[] args) {
        Map<String, String> appParams = new HashMap<>();
        for (int i = 0; args != null && i < args.length; i++) {
            if (args[i].contains("=")) {
                String[] kv = args[i].split("=", 2);
                appParams.put(kv[0], kv[1]);
            }
        }
        return appParams;
    }
}
