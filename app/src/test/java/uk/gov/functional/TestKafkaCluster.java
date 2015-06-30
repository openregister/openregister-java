package uk.gov.functional;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.TestUtils;
import kafka.utils.VerifiableProperties;
import org.apache.curator.test.TestingServer;
import scala.collection.Iterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Copied from <a href="https://cwiki.apache.org/confluence/display/KAFKA/FAQ#FAQ-Unittesting">kafka FAQ</a>
 */
public class TestKafkaCluster {

    private final TestingServer zkServer;
    private final KafkaServerStartable kafkaServer;

    public TestKafkaCluster(int zkPort) throws Exception {
        zkServer = new TestingServer(zkPort);
        KafkaConfig config = getKafkaConfig(zkServer.getConnectString());
        kafkaServer = new KafkaServerStartable(config);
        kafkaServer.startup();
    }

    private KafkaConfig getKafkaConfig(String zkConnectString) {
        Iterator<Properties> propsIterator = TestUtils.createBrokerConfigs(1, false).iterator();
        assert propsIterator.hasNext();
        Properties props = propsIterator.next();
        assert props.containsKey("zookeeper.connect");
        props.put("zookeeper.connect", zkConnectString);
        props.put("port","6001");
        System.out.println("kafka props: " + props);
        return new KafkaConfig(props);
    }

    public void stop() throws IOException {
        kafkaServer.shutdown();
        zkServer.stop();
    }

    public List<String> readMessages(String topicName, int expectedMessages) throws TimeoutException {
        ExecutorService singleThread = Executors.newSingleThreadExecutor();
        Properties consumerProperties = new Properties();
        consumerProperties.put("zookeeper.connect", zkServer.getConnectString());
        consumerProperties.put("group.id", "10");
        consumerProperties.put("socket.timeout.ms", "500");
        consumerProperties.put("consumer.id", "test");
        consumerProperties.put("auto.offset.reset", "smallest");
        ConsumerConnector javaConsumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(consumerProperties));
        StringDecoder stringDecoder = new StringDecoder(new VerifiableProperties(new Properties()));
        Map<String, Integer> topicMap = new HashMap<>();
        topicMap.put(topicName, 1);
        Map<String, List<KafkaStream<String, String>>> events = javaConsumerConnector.createMessageStreams(topicMap, stringDecoder, stringDecoder);
        List<KafkaStream<String, String>> events1 = events.get(topicName);
        KafkaStream<String, String> kafkaStreams = events1.get(0);

        Future<List<String>> submit = singleThread.submit(() -> {
            List<String> messages = new ArrayList<>();
            ConsumerIterator<String, String> iterator = kafkaStreams.iterator();
            while (messages.size() != expectedMessages && iterator.hasNext()) {
                String message = iterator.next().message();
                messages.add(message);
            }
            return messages;
        });

        try {
            return submit.get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new TimeoutException("Timed out waiting for messages");
        } finally {
            singleThread.shutdown();
            javaConsumerConnector.shutdown();
        }
    }
}
