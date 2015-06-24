package uk.gov;

import com.google.common.base.Strings;
import uk.gov.mint.RabbitMQConnector;
import uk.gov.store.PostgresDataStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Application {

    @SuppressWarnings("FieldCanBeLocal")
    private static RabbitMQConnector notToBeGcedMQConnector;

    public static void main(String[] args) throws InterruptedException, IOException {

        Map<String, String> propertiesMap = createConfigurationMap(args);

        Properties properties = new Properties();
        properties.load(configurationPropertiesStream(propertiesMap.get("config.file")));
        properties.putAll(propertiesMap);

        String pgConnectionString = properties.getProperty("postgres.connection.string");
        String storeName = properties.getProperty("store.name");
        consoleLog("Connecting to Postgres database: " + pgConnectionString);

        notToBeGcedMQConnector = new RabbitMQConnector(new PostgresDataStore(pgConnectionString, storeName));
        notToBeGcedMQConnector.connect(properties);

        consoleLog("Application started...");

        Thread.currentThread().join();
    }

    private static InputStream configurationPropertiesStream(String fileName) throws IOException {
        if (Strings.isNullOrEmpty(fileName)) {
            consoleLog("Configuration properties file not provided, using default application.properties file");
            return Application.class.getResourceAsStream("/application.properties");
        } else {
            consoleLog("Loading properties file: " + fileName);
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

    private static void consoleLog(String logMessage) {
        System.out.println(logMessage);
    }

}

