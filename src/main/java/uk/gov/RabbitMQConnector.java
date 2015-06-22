package uk.gov;

import com.rabbitmq.client.*;
import uk.gov.store.DataStore;

import java.util.Collections;

public class RabbitMQConnector {
    private final DataStore dataStore;

    public RabbitMQConnector(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void connect(String connectionString, String queue, String exchange) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(connectionString);
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();

            AMQP.Exchange.DeclareOk declareExchange = channel.exchangeDeclare(exchange, "direct");
            AMQP.Queue.DeclareOk declareQueue = channel.queueDeclare(queue, true, false, false, Collections.<String, Object>emptyMap());
            AMQP.Queue.BindOk bindOk = channel.queueBind(queue, exchange, "register-queue-routing-key");


            Consumer consumer = new MessageHandler(channel, dataStore);
            channel.basicConsume(queue, consumer);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


}
