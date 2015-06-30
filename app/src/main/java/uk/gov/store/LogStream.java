package uk.gov.store;

import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;

public class LogStream {
    private final Producer<String, byte[]> producer;
    public static final String TOPIC_NAME = "register";

    public LogStream(String bootstrapServers) {
        this.producer = new KafkaProducer<>(ImmutableMap.of(
                "bootstrap.servers", bootstrapServers,
                "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer"
        ));
    }

    public void send(byte[] message) {
        producer.send(new ProducerRecord<>(TOPIC_NAME, "primaryKey", message));
    }

    public void close() throws IOException {
        producer.close();
    }
}
