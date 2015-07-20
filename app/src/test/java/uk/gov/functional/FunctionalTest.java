package uk.gov.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.Application;
import uk.gov.mint.CanonicalJsonMapper;
import uk.gov.store.EntriesQueryDAO;
import uk.gov.store.EntriesUpdateDAO;
import uk.gov.store.HighWaterMarkDAO;

import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FunctionalTest {
    private java.sql.Connection pgConnection;
    private Application application;
    private TestKafkaCluster testKafkaCluster;
    private Properties properties;

    @Before
    public void setup() throws Exception {
        properties = new Properties();
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
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

        String messageString = String.format("{\"name\":\"ft_mint_test\",\"test\":\"test_%s\"}",
                String.valueOf(System.currentTimeMillis()));
        byte[] message = messageString.getBytes();
        JsonNode messageJson = canonicalJsonMapper.readFromBytes(message);

        waitForMessageToBeConsumed();
        send(
                javaslang.collection.List.of(messageString)
        );

        waitForMessageToBeConsumed();

        byte[] actual = tableRecord();
        String actualString = new String(actual);
        final JsonNode actualJson = canonicalJsonMapper.readFromBytes(actual);

        JsonNode entryNode = actualJson.get("entry");
        assertThat(actualJson.get("hash"), notNullValue(JsonNode.class));
        assertThat(entryNode, notNullValue(JsonNode.class));

        assertThat(entryNode.get("name").textValue(),
                equalTo(messageJson.get("name").textValue()));
        assertThat(entryNode.get("test").textValue(),
                equalTo(messageJson.get("test").textValue()));

        List<String> messages = testKafkaCluster.readMessages("register", 1);
        assertThat(messages, is(Collections.singletonList(actualString)));
    }

    private void send(javaslang.collection.List<String> payload) {
        try {
            final String mintUrl = properties.getProperty("mintUrl");
            Response r = new JdkRequest(mintUrl)
                    .method(Request.POST)
                    .body()
                    .set(payload.join("\n"))
                    .back()
                    .fetch();
            if (r.status() != 200)
                System.err.println("Unexpected result: " + r.body());
            else
                System.out.println("Loaded " + 1000 + " entries...");
        } catch (Exception e) {
            System.err.println("Error occurred sending data to mint: " + e);
        }
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
            statement.execute("DROP TABLE IF EXISTS " + EntriesUpdateDAO.tableName);
            statement.execute("DROP TABLE IF EXISTS " + HighWaterMarkDAO.tableName);
        }
    }
}
