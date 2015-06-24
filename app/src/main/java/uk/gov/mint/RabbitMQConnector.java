package uk.gov.mint;

import com.rabbitmq.client.*;
import uk.gov.integration.DataStoreApplication;

import java.util.Collections;
import java.util.Properties;

public class RabbitMQConnector {
    private final DataStoreApplication dataStoreApplication;

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
            factory.setUri(connectionString);
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();

            AMQP.Exchange.DeclareOk declareExchange = channel.exchangeDeclare(exchange, "direct");
            AMQP.Queue.DeclareOk declareQueue = channel.queueDeclare(queue, true, false, false, Collections.<String, Object>emptyMap());
            AMQP.Queue.BindOk bindOk = channel.queueBind(queue, exchange, routingKey);

            Consumer consumer = new MessageHandler(channel, dataStoreApplication);
            channel.basicConsume(queue, consumer);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
