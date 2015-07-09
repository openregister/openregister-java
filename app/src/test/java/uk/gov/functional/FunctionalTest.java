package uk.gov.functional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.Application;
import uk.gov.store.EntriesQueryDAO;
import uk.gov.store.HighWaterMarkDAO;

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

@Ignore
public class FunctionalTest {
    private java.sql.Connection pgConnection;
    private Application application;
    private TestKafkaCluster testKafkaCluster;

    @Before
    public void setup() throws Exception {
        Properties properties = new Properties();
        String configFilename = FunctionalTest.class.getResource("/test-application.properties").getFile();
        InputStream resourceAsStream = FunctionalTest.class.getResourceAsStream("/test-application.properties");
        properties.load(resourceAsStream);

        pgConnection = DriverManager.getConnection(properties.getProperty("postgres.connection.string"));

        cleanDatabase();

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
        // TODO: Change this to use HTTP connection
//        connection.createChannel().basicPublish(exchange, routingKey, new AMQP.BasicProperties(), message);

        waitForMessageToBeConsumed();
        assertThat(tableRecord(), is(message));

        List<String> messages = testKafkaCluster.readMessages("register", 1);
        assertThat(messages, is(Collections.singletonList(messageString)));
    }

    private byte[] tableRecord() throws SQLException {
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("SELECT ENTRY FROM " + EntriesQueryDAO.tableName);
            ResultSet resultSet = statement.getResultSet();
            return resultSet.next() ? resultSet.getBytes(1) : null;
        }
    }

    private void waitForMessageToBeConsumed() throws InterruptedException {
        Thread.sleep(500);
    }

    private void cleanDatabase() throws SQLException {
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS " + EntriesQueryDAO.tableName);
            statement.execute("DROP TABLE IF EXISTS " + HighWaterMarkDAO.tableName);
        }
    }
}
