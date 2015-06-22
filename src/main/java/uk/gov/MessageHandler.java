package uk.gov;

import com.rabbitmq.client.*;
import uk.gov.store.DataStore;

import java.io.IOException;

class MessageHandler extends DefaultConsumer {

    private final Channel channel;
    private final DataStore dataStore;

    public MessageHandler(Channel channel, DataStore dataStore) {
        super(channel);
        this.channel = channel;
        this.dataStore = dataStore;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        dataStore.add(body);
        channel.basicAck(envelope.getDeliveryTag(), false);
    }
}
