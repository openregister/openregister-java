package uk.gov.mint;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import uk.gov.integration.DataStoreApplication;

import java.io.IOException;

class MessageHandler extends DefaultConsumer {

    private final Channel channel;
    private final DataStoreApplication dataStoreApplication;

    public MessageHandler(Channel channel, DataStoreApplication dataStoreApplication) {
        super(channel);
        this.channel = channel;
        this.dataStoreApplication = dataStoreApplication;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        dataStoreApplication.add(body);
        channel.basicAck(envelope.getDeliveryTag(), false);
    }
}
