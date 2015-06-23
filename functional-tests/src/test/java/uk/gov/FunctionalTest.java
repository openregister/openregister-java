package uk.gov;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
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

    @Before
    public void setup() throws SQLException, IOException {
        Properties properties = new Properties();
        properties.load(FunctionalTest.class.getResourceAsStream("/test-application.properties"));

        exchange = properties.getProperty("rabbitmq.exchange");
        routingKey = properties.getProperty("rabbitmq.exchange.routing.key");
        queue = properties.getProperty("rabbitmq.queue");
        rabbitMQConnectionString = properties.getProperty("rabbitmq.connection.string");

        pgConnection = DriverManager.getConnection(properties.getProperty("postgres.connection.string"));
        cleanDatabase();
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
            statement.execute("SELECT * FROM STORE");
            ResultSet resultSet = statement.getResultSet();
            return resultSet.next() ? resultSet.getBytes(1) : null;
        }
    }

    private void waitForMessageToBeConsumed() throws InterruptedException {
        Thread.sleep(100);
    }

    private void cleanDatabase() throws SQLException {
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("DELETE FROM STORE");
        }
    }
}
