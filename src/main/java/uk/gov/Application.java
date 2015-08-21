package uk.gov;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Application {

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        Map<String, String> propertiesMap = createConfigurationMap(args);

        Properties properties = new Properties();
        properties.load(configurationPropertiesStream(propertiesMap.get("config.file")));

        SourcePostgresDB sourceDB = new SourcePostgresDB(properties.getProperty("source.postgres.db.connectionString"));
        DestinationPostgresDB destinationDB = new DestinationPostgresDB(properties.getProperty("destination.postgres.db.connectionString"));

        Indexer indexer = new Indexer(sourceDB, destinationDB);

        //noinspection InfiniteLoopStatement
        while (true) {
            ConsoleLogger.log("Starting index update...");
            indexer.update();
            ConsoleLogger.log("Index update completed.");
            ConsoleLogger.log("Waiting 10 seconds......");
            Thread.sleep(10000);
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
