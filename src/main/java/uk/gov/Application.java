package uk.gov;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import uk.gov.store.DataStore;
import uk.gov.store.PostgresDataStore;

public class Application {

    public static void main(String[] args) throws ConfigurationException, InterruptedException {

        //TODO: use application arguments to take properties
        String fileName = null;

        if (StringUtils.isEmpty(fileName)) {
            System.out.println("Properties file name not provided, using default application.properties file");
            fileName = "application.properties";
        }

        PropertiesConfiguration configuration = new PropertiesConfiguration(fileName);

        String connectionString = configuration.getString("rabbitmq.connection.string");
        String queue = configuration.getString("rabbitmq.queue");
        String exchange = configuration.getString("rabbitmq.exchange");

        DataStore dataStore = new PostgresDataStore(configuration.getString("postgres.connection.string"));

        new RabbitMQConnector(dataStore).connect(connectionString, queue, exchange);

        System.out.println("Application started...");

        Thread.currentThread().join();
    }

}

