package uk.gov.mint.monitoring;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class CloudWatchHeartbeater implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(CloudWatchHeartbeater.class);

    private String environment;
    private final AmazonCloudWatch acw;

    public CloudWatchHeartbeater(String environment) {
        this.environment = environment;

        acw = new AmazonCloudWatchClient();
        acw.setEndpoint("https://monitoring.eu-west-1.amazonaws.com");
    }

    @Override
    public void run() {
        LOGGER.info("Cloudwatch heartbeat started");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                performHeartbeat();

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    // Reset the flag so we exit the loop
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
                break;
            }
        }

        LOGGER.info("Cloudwatch heartbeat ended");
    }

    private void performHeartbeat() {
        acw.putMetricData(new PutMetricDataRequest()
                .withNamespace(environment)
                .withMetricData(Collections.singletonList(new MetricDatum()
                        .withMetricName("heartbeat")
                        .withDimensions(new Dimension().withName("application").withValue("mint"))
                        .withValue(1.0))
                )
        );
    }
}
