package uk.gov.register.presentation.functional;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import uk.gov.register.presentation.app.PresentationApplication;
import uk.gov.register.presentation.config.PresentationConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.List;

public class FunctionalTestBase {
    public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/ft_presentation";

    public static final String TOPIC = "register";
    private static final TestKafkaCluster testKafkaCluster = new TestKafkaCluster(TOPIC);

    @ClassRule
    public static final TestRule cleanDb = new CleanDatabaseRule(DATABASE_URL, "ordered_entry_index");

    @ClassRule
    public static final DropwizardAppRule<PresentationConfiguration> RULE =
            new DropwizardAppRule<>(PresentationApplication.class,
                    ResourceHelpers.resourceFilePath("test-app-config.yaml"),
                    ConfigOverride.config("zookeeperServer", "localhost:" + testKafkaCluster.getZkPort()),
                    ConfigOverride.config("database.url", DATABASE_URL));

    protected static Client client;

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");
    }

    Response getRequest(String path) {
        return client.target(String.format("http://localhost:%d%s", RULE.getLocalPort(), path))
                .request()
                .get();
    }

    static void publishMessages(List<String> messages) {
        for (String message : messages) {
            testKafkaCluster.getProducer().send(new ProducerRecord<>(TOPIC, message.getBytes()));
        }
        waitForAppToConsumeMessage();
    }

    private static void waitForAppToConsumeMessage() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
