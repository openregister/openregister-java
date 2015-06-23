package uk.gov;

import com.rabbitmq.client.*;
import org.apache.commons.configuration.PropertiesConfiguration;
import uk.gov.store.DataStore;

import java.util.Collections;

public class RabbitMQConnector {
    private final DataStore dataStore;

    public RabbitMQConnector(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void connect(PropertiesConfiguration configuration) {
        try {
            String connectionString = configuration.getString("rabbitmq.connection.string");
            String queue = configuration.getString("rabbitmq.queue");
            String exchange = configuration.getString("rabbitmq.exchange");
            String routingKey = configuration.getString("rabbitmq.exchange.routing.key");

            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(connectionString);
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();

            AMQP.Exchange.DeclareOk declareExchange = channel.exchangeDeclare(exchange, "direct");
            AMQP.Queue.DeclareOk declareQueue = channel.queueDeclare(queue, true, false, false, Collections.<String, Object>emptyMap());
            AMQP.Queue.BindOk bindOk = channel.queueBind(queue, exchange, routingKey);

            Consumer consumer = new MessageHandler(channel, dataStore);
            channel.basicConsume(queue, consumer);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
