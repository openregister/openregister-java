package uk.gov.register.presentation.functional;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.presentation.PresentationApplication;
import uk.gov.register.presentation.PresentationConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FunctionalTest {
    public static final String TOPIC = "register";

    private static final TestKafkaCluster testKafkaCluster = new TestKafkaCluster(TOPIC);

    @ClassRule
    public static final DropwizardAppRule<PresentationConfiguration> RULE =
            new DropwizardAppRule<>(PresentationApplication.class,
                    ResourceHelpers.resourceFilePath("test-app-config.yaml"),
                    ConfigOverride.config("zookeeperServer","localhost:" + testKafkaCluster.getZkPort()));
    private final Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

    @Test
    public void shouldConsumeMessageFromKafkaAndShowAsLatest() throws Exception {
        String message = "{\"foo\":\"bar\"}";
        testKafkaCluster.getProducer().send(new ProducerRecord<>(TOPIC, message.getBytes()));
        waitForAppToConsumeMessage();

        Response response = client.target(String.format("http://localhost:%d/latest.json", RULE.getLocalPort())).request().get();

        assertThat(response.readEntity(String.class), equalTo("[{\"foo\":\"bar\"}]"));

    }

    private void waitForAppToConsumeMessage() throws InterruptedException {
        Thread.sleep(3000);
    }

}
