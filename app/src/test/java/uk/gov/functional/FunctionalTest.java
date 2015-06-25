package uk.gov.functional;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
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
import java.util.Properties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FunctionalTest {
    private String rabbitMQConnectionString;
    private String exchange;
    private String routingKey;
    private String queue;
    private java.sql.Connection pgConnection;
    private Application application;

    @Before
    public void setup() throws SQLException, IOException {
        Properties properties = new Properties();
        String configFilename = FunctionalTest.class.getResource("/test-application.properties").getFile();
        InputStream resourceAsStream = FunctionalTest.class.getResourceAsStream("/test-application.properties");
        properties.load(resourceAsStream);

        exchange = properties.getProperty("rabbitmq.exchange");
        routingKey = properties.getProperty("rabbitmq.exchange.routing.key");
        queue = properties.getProperty("rabbitmq.queue");
        rabbitMQConnectionString = properties.getProperty("rabbitmq.connection.string");

        pgConnection = DriverManager.getConnection(properties.getProperty("postgres.connection.string"));
        cleanDatabase();

        application = new Application("config.file=" + configFilename);
        application.startup();
    }

    @After
    public void tearDown() throws Exception {
        application.shutdown();
    }

    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(rabbitMQConnectionString);
        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();

        AMQP.Queue.BindOk bindOk = channel.queueBind(queue, exchange, routingKey);

        byte[] message = String.valueOf(System.currentTimeMillis()).getBytes();
        channel.basicPublish(exchange, routingKey, new AMQP.BasicProperties(), message);

        waitForMessageToBeConsumed();
        assertThat(tableRecord(), is(message));
    }

    private byte[] tableRecord() throws SQLException {
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("SELECT ENTRY FROM FUNCTIONAL_TESTS_STORE");
            ResultSet resultSet = statement.getResultSet();
            return resultSet.next() ? resultSet.getBytes(1) : null;
        }
    }

    private void waitForMessageToBeConsumed() throws InterruptedException {
        Thread.sleep(100);
    }

    private void cleanDatabase() throws SQLException {
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("DELETE FROM FUNCTIONAL_TESTS_STORE");
        }
    }
}
