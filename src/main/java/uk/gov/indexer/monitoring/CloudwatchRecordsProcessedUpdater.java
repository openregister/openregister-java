package uk.gov.indexer.monitoring;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;

import java.util.Collections;

public class CloudwatchRecordsProcessedUpdater {
    private String environment;
    private String register;
    private final AmazonCloudWatch acw;

    public CloudwatchRecordsProcessedUpdater(String environment, String register) {
        this.environment = environment;
        this.register = register;

        acw = new AmazonCloudWatchClient();
        acw.setEndpoint("https://monitoring.eu-west-1.amazonaws.com");
    }

    public void update(long recordsProcessed) {
        acw.putMetricData(new PutMetricDataRequest()
                .withNamespace(environment)
                .withMetricData(Collections.singletonList(new MetricDatum()
                        .withMetricName("recordsProcessed")
                        .withDimensions(new Dimension().withName("application").withValue("indexer"))
                        .withDimensions(new Dimension().withName("register").withValue(register))
                        .withUnit("Count")
                        .withValue((double) recordsProcessed))
                )
        );
    }
}
