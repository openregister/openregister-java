package uk.gov.mint;

import com.rabbitmq.client.*;
import uk.gov.integration.DataStoreApplication;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnector {
    private final DataStoreApplication dataStoreApplication;
    private Channel channel;

    public RabbitMQConnector(DataStoreApplication dataStoreApplication) {
        this.dataStoreApplication = dataStoreApplication;
    }

    public void connect(Properties configuration) {
        try {
            String connectionString = configuration.getProperty("rabbitmq.connection.string");
            String queue = configuration.getProperty("rabbitmq.queue");
            String exchange = configuration.getProperty("rabbitmq.exchange");
            String routingKey = configuration.getProperty("rabbitmq.exchange.routing.key");

            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(resolveConnectionString(connectionString));
            Connection conn = factory.newConnection();
            channel = conn.createChannel();

            AMQP.Exchange.DeclareOk declareExchange = channel.exchangeDeclare(exchange, "direct");
            AMQP.Queue.DeclareOk declareQueue = channel.queueDeclare(queue, true, false, false, Collections.<String, Object>emptyMap());
            AMQP.Queue.BindOk bindOk = channel.queueBind(queue, exchange, routingKey);

            Consumer consumer = new MessageHandler(channel, dataStoreApplication);
            channel.basicConsume(queue, consumer);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private String resolveConnectionString(final String connectionString) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final Map<String, String> env = System.getenv();
        if(!env.containsKey("RABBITMQ_PORT_5672_TCP_ADDR")) {
            System.out.println("RABBITMQ_PORT_5672_TCP_ADDR _NOT_ defined - using default: " + connectionString);
            return connectionString;
        }

        final String dockerConnectionString = "amqp://"
                + env.get("RABBITMQ_PORT_5672_TCP_ADDR") + ":"
                + env.get("RABBITMQ_PORT_5672_TCP_PORT");
        System.out.println("RABBITMQ_PORT_5672_TCP_ADDR defined - using: " + dockerConnectionString);

        return dockerConnectionString;
    }

    public void close() throws IOException, TimeoutException {
        channel.close();
    }
}
