package uk.gov.indexer.monitoring;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;

import java.util.Collections;

public class CloudwatchProcessHeartbeat implements Runnable {
    private String environment;
    private final AmazonCloudWatch acw;

    public CloudwatchProcessHeartbeat(String environment) {
        this.environment = environment;

        acw = new AmazonCloudWatchClient();
        acw.setEndpoint("https://monitoring.eu-west-1.amazonaws.com");
    }

    @Override
    public void run() {
        acw.putMetricData(new PutMetricDataRequest()
                        .withNamespace(environment)
                        .withMetricData(Collections.singletonList(new MetricDatum()
                                        .withMetricName("heartbeat")
                                        .withDimensions(new Dimension().withName("application").withValue("indexer"))
                                        .withValue(1.0))
                        )
        );
    }
}
