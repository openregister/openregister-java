package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import uk.gov.register.presentation.functional.testSupport.TestKafkaCluster;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class EndToEndTest extends FunctionalTestBase {

    static void publishMessagesToKafka(List<String> messages) {
        Producer<String, byte[]> producer = TestKafkaCluster.getProducer();
        for (String message : messages) {
            producer.send(new ProducerRecord<>("register", message.getBytes()));
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

    @Test
    public void shouldConsumeMessageFromKafkaAndShowAsFeeds() throws Exception {
        publishMessagesToKafka(ImmutableList.of(
                "{\"hash\":\"entryHash1\", \"entry\":{\"ft_test_pkey\":\"ft_test_pkey_value_1\", \"key1\":\"key1Value_1\"}}",
                "{\"hash\":\"entryHash2\", \"entry\":{\"ft_test_pkey\":\"ft_test_pkey_value_2\", \"key1\":\"key1Value_2\"}}"
        ));

        Response response = client.target("http://localhost:9000/feed.json").request().get();

        assertThat(response.readEntity(String.class), equalTo("[{\"hash\":\"entryHash2\",\"entry\":{\"key1\":\"key1Value_2\",\"ft_test_pkey\":\"ft_test_pkey_value_2\"}},{\"hash\":\"entryHash1\",\"entry\":{\"key1\":\"key1Value_1\",\"ft_test_pkey\":\"ft_test_pkey_value_1\"}}]"));

    }


}

