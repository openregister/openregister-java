package uk.gov.admin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class RabbitMQPublisher implements AutoCloseable {
    private final String connectionString;
    private final String exchange;
    private final String routingKey;

    private Channel channel;

    public RabbitMQPublisher(Properties configuration) {
        connectionString = configuration.getProperty("rabbitmq.connection.string");
        exchange = configuration.getProperty("rabbitmq.exchange");
        routingKey = configuration.getProperty("rabbitmq.exchange.routing.key");


        try {
            channel = prepareConnection();
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException | TimeoutException | IOException e) {
            throw new RuntimeException("Could not create connection to RabbitMQ", e);
        }
    }

    public void publish(List<String> listOfData) {
        try {
            final String batchData = listOfData.stream().collect(Collectors.joining(","));
            final String batchDataDocument = "[" + batchData + "]";
            channel.basicPublish(exchange, routingKey, null, batchDataDocument.getBytes());
        } catch (NullPointerException e) {
            throw new RuntimeException("Did you call prepareConnection?", e);
        } catch (Throwable t) {
            throw new RuntimeException("Error occurred publishing datafile to queue", t);
        }
    }

    private Channel prepareConnection() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(connectionString);
        Connection conn = factory.newConnection();
        return conn.createChannel();
    }

    @Override
    public void close() throws Exception {
        channel.close();
        channel.getConnection().close();
    }
}
