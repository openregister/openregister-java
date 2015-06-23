package uk.gov;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import uk.gov.store.PostgresDataStore;

import java.util.HashMap;
import java.util.Map;

public class Application {

    @SuppressWarnings("FieldCanBeLocal")
    private static RabbitMQConnector rabbitMQConnector;

    public static void main(String[] args) throws ConfigurationException, InterruptedException {

        Map<String, String> propertiesMap = createConfigurationMap(args);

        String fileName = propertiesMap.get("config.file");

        if (StringUtils.isEmpty(fileName)) {
            System.out.println("Properties file name not provided, using default application.properties file");
            fileName = "application.properties";
        }

        PropertiesConfiguration configuration = new PropertiesConfiguration(fileName);

        rabbitMQConnector = new RabbitMQConnector(new PostgresDataStore(configuration.getString("postgres.connection.string")));
        rabbitMQConnector.connect(configuration);

        System.out.println("Application started...");

        Thread.currentThread().join();
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

