package uk.gov.register.presentation.functional.testSupport;

import com.google.common.base.Throwables;
import kafka.admin.AdminUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.TestUtils;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Properties;

public class TestKafkaCluster {

    private final TestingServer zkServer;
    @SuppressWarnings("FieldCanBeLocal")
    private final KafkaServerStartable kafkaServer;

    private static final int zkServerPort = 7890;
    private static final int kafkaPort = 61789;

    public TestKafkaCluster(String topicToCreate) {
        try {
            zkServer = new TestingServer(getZkPort());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        kafkaServer = new KafkaServerStartable(makeKafkaConfig("localhost:" + getZkPort()));
        kafkaServer.startup();
        // ensure topic exists before application connects
        createTopic(topicToCreate);
    }



    private void createTopic(String name) {
        ZkClient zkClient = new ZkClient(zkServer.getConnectString(), 500, 500, ZKStringSerializer$.MODULE$);
        AdminUtils.createTopic(zkClient, name, 1, 1, new Properties());
        zkClient.close();
    }

    private KafkaConfig makeKafkaConfig(Object zkConnectString) {
        Properties props = TestUtils.createBrokerConfigs(1, false).head();
        props.put("zookeeper.connect", zkConnectString);
        props.put("port", String.valueOf(kafkaPort));
        return new KafkaConfig(props);
    }

    public int getZkPort() {
        return zkServerPort;
    }

    private static Properties kafkaProducerProperties() {
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", "localhost:" + kafkaPort);
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        return producerProps;
    }

    public static Producer<String, byte[]> getProducer() {
        return new KafkaProducer<>(kafkaProducerProperties());
    }

}
