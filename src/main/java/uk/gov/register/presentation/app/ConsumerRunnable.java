package uk.gov.register.presentation.app;

import com.google.common.collect.ImmutableMap;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import uk.gov.register.presentation.config.ZookeeperConfiguration;
import uk.gov.register.presentation.dao.RecentEntryIndexUpdateDAO;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConsumerRunnable implements Runnable {

    private final Properties properties;
    public static final String TOPIC_NAME = "register";
    private final RecentEntryIndexUpdateDAO updateDAO;

    public ConsumerRunnable(ZookeeperConfiguration zkConfig, RecentEntryIndexUpdateDAO updateDAO) {
        this.updateDAO = updateDAO;
        properties = new Properties();
        properties.put("zookeeper.connect", zkConfig.getZookeeperServer());
        properties.put("zookeeper.session.timeout.ms", "1000");
        properties.put("zookeeper.sync.time.ms", "200");
        properties.put("group.id", "debug"); // should be unique to this presentation app
        properties.put("enable.auto.commit", "false");
        properties.put("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        properties.put("value.deserializer", org.apache.kafka.common.serialization.ByteArrayDeserializer.class);
        properties.put("partition.assignment.strategy", "range");

        updateDAO.ensureTableExists();
    }

    @Override
    public void run() {
        ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));
        StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
        Map<String, List<KafkaStream<String, byte[]>>> messageStreams = consumerConnector.createMessageStreams(ImmutableMap.of(TOPIC_NAME, 1), keyDecoder, bytes -> bytes);
        KafkaStream<String, byte[]> kafkaStream = messageStreams.get(TOPIC_NAME).get(0);
        for (MessageAndMetadata<String, byte[]> messageAndMetadata : kafkaStream) {
            byte[] message = messageAndMetadata.message();
            updateDAO.append(message);
        }
    }
}
