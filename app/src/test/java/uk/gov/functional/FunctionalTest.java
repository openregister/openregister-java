package uk.gov.functional;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.Application;

import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FunctionalTest {
    private String exchange;
    private String routingKey;
    private String queue;
    private java.sql.Connection pgConnection;
    private Application application;
    private TestKafkaCluster testKafkaCluster;
    private Connection connection;

    @Before
    public void setup() throws Exception {
        Properties properties = new Properties();
        String configFilename = FunctionalTest.class.getResource("/test-application.properties").getFile();
        InputStream resourceAsStream = FunctionalTest.class.getResourceAsStream("/test-application.properties");
        properties.load(resourceAsStream);

        exchange = properties.getProperty("rabbitmq.exchange");
        routingKey = properties.getProperty("rabbitmq.exchange.routing.key");
        queue = properties.getProperty("rabbitmq.queue");
        String rabbitMQConnectionString = properties.getProperty("rabbitmq.connection.string");

        pgConnection = DriverManager.getConnection(properties.getProperty("postgres.connection.string"));

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(rabbitMQConnectionString);
        connection = factory.newConnection();

        cleanDatabase();
        cleanQueue();

        testKafkaCluster = new TestKafkaCluster(6000); // must match test-application.properties

        application = new Application("config.file=" + configFilename);
        application.startup();
    }

    @After
    public void tearDown() throws Exception {
        application.shutdown();
        testKafkaCluster.stop();
    }

    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {

        String messageString = String.format("{\"time\":%s}", String.valueOf(System.currentTimeMillis()));
        byte[] message = messageString.getBytes();
        connection.createChannel().basicPublish(exchange, routingKey, new AMQP.BasicProperties(), message);

        waitForMessageToBeConsumed();
        assertThat(tableRecord(), is(message));

        List<String> messages = testKafkaCluster.readMessages("register", 1);
        assertThat(messages, is(Collections.singletonList(messageString)));
    }

    private byte[] tableRecord() throws SQLException {
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("SELECT ENTRY FROM FUNCTIONAL_TESTS_STORE");
            ResultSet resultSet = statement.getResultSet();
            return resultSet.next() ? resultSet.getBytes(1) : null;
        }
    }

    private void waitForMessageToBeConsumed() throws InterruptedException {
        Thread.sleep(500);
    }

    private void cleanQueue() throws Exception {
        try {
            // purge the queue if it exists; throws IOException if not
            connection.createChannel().queuePurge(queue);
        } catch (IOException e) {
            // don't care, continue
        }
    }

    private void cleanDatabase() throws SQLException {
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS FUNCTIONAL_TESTS_STORE");
            statement.execute("DROP TABLE IF EXISTS STREAMED_ENTRIES");
        }
    }
}
