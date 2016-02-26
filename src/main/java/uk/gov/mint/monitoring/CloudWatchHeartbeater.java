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
    private String register;
    private final AmazonCloudWatch acw;

    public CloudWatchHeartbeater(String environment, String register) {
        this.environment = environment;
        this.register = register;

        acw = new AmazonCloudWatchClient();
        acw.setEndpoint("https://monitoring.eu-west-1.amazonaws.com");
    }

    @Override
    public void run() {
        acw.putMetricData(new PutMetricDataRequest()
                .withNamespace(environment)
                .withMetricData(Collections.singletonList(new MetricDatum()
                        .withMetricName("heartbeat")
                        .withDimensions(new Dimension().withName("application").withValue("mint"))
                        .withDimensions(new Dimension().withName("register").withValue(register))
                        .withValue(1.0))
                )
        );
    }
}
