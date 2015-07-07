package uk.gov.mint;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readValue(body, JsonNode.class);
        } catch (JsonParseException e) {
            // ACK message as we know it's unprocessable
            channel.basicAck(envelope.getDeliveryTag(), false);
            // FIXME: should report this failure somewhere
            return;
        }

        dataStoreApplication.add(canonicalize(jsonNode));
        channel.basicAck(envelope.getDeliveryTag(), false);
    }

    private byte[] canonicalize(JsonNode jsonNode) throws JsonProcessingException {
        // Method from http://stackoverflow.com/questions/18952571/jackson-jsonnode-to-string-with-sorted-keys
        Object obj = objectMapper.treeToValue(jsonNode, Object.class);
        // for some reason, writeValueAsString(obj).getBytes() doesn't re-escape unicode, but writeValueAsBytes does
        // our canonical form requires raw unescaped unicode, so we need this version
        return objectMapper.writeValueAsString(obj).getBytes();
    }
}
