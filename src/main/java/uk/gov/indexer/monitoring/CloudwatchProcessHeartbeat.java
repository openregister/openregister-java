package uk.gov.indexer.monitoring;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.ExceptionFormatter;

import java.util.Collections;

public class CloudwatchProcessHeartbeat implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(CloudwatchProcessHeartbeat.class);

    private String environment;
    private final AmazonCloudWatch acw;

    public CloudwatchProcessHeartbeat(String environment) {
        this.environment = environment;

        acw = new AmazonCloudWatchClient();
        acw.setEndpoint("https://monitoring.eu-west-1.amazonaws.com");
    }

    @Override
    public void run() {
        try {
            acw.putMetricData(new PutMetricDataRequest()
                    .withNamespace(environment)
                    .withMetricData(Collections.singletonList(new MetricDatum()
                            .withMetricName("heartbeat")
                            .withDimensions(new Dimension().withName("application").withValue("indexer"))
                            .withValue(1.0))
                    )
            );
        } catch (Exception e) {
            LOGGER.error(ExceptionFormatter.formatExceptionAsString(e));
        }
    }
}
