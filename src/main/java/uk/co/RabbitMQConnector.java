package uk.co;

import com.rabbitmq.client.*;

import java.util.Collections;

public class RabbitMQConnector {
    public static void connect(String connectionString, String queue, String exchange) {
        //TODO: close the queue connection at shutdown
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(connectionString);
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();

            AMQP.Exchange.DeclareOk declareExchange = channel.exchangeDeclare(exchange, "direct");
            AMQP.Queue.DeclareOk declareQueue = channel.queueDeclare(queue, true, false, false, Collections.<String, Object>emptyMap());

            AMQP.Queue.BindOk bindOk = channel.queueBind(queue, exchange, "register-queue-routing-key");


            Consumer consumer = new MessageHandler(channel, new DataStoreImpl());
            channel.basicConsume(queue, consumer);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


}
