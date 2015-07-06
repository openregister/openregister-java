package uk.gov.mint;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import uk.gov.integration.DataStoreApplication;

import java.io.IOException;

class MessageHandler extends DefaultConsumer {

    private final Channel channel;
    private final DataStoreApplication dataStoreApplication;
    private final ObjectMapper objectMapper;

    public MessageHandler(Channel channel, DataStoreApplication dataStoreApplication) {
        super(channel);
        this.channel = channel;
        this.dataStoreApplication = dataStoreApplication;
        objectMapper = new ObjectMapper();
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        try {
            objectMapper.readValue(body, JsonNode.class);
        } catch (JsonParseException e) {
            // ACK message as we know it's unprocessable
            channel.basicAck(envelope.getDeliveryTag(), false);
            // FIXME: should report this failure somewhere
            return;
        }

        dataStoreApplication.add(body);
        channel.basicAck(envelope.getDeliveryTag(), false);
    }
}
