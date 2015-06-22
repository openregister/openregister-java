package uk.gov;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

public class Application {

    public static void main(String[] args) throws ConfigurationException, InterruptedException {

        //TODO: use application arguments to take properties
        String fileName = null;

        if (StringUtils.isEmpty(fileName)) {
            System.out.println("Properties file name not provided, using default application.properties file");
            fileName = "application.properties";
        }

        PropertiesConfiguration configuration = new PropertiesConfiguration(fileName);

        String connectionString = configuration.getString("connection-string");
        String queue = configuration.getString("queue");
        String exchange = configuration.getString("exchange");

        RabbitMQConnector.connect(connectionString, queue, exchange);

        System.out.println("Application started...");

        Thread.currentThread().join();
    }

}

