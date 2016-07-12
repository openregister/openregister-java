package uk.gov.register.test_support;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import uk.gov.RegisterApplication;

import javax.ws.rs.client.Client;

import static javax.validation.Validation.buildDefaultValidatorFactory;

public abstract class TestJerseyClientBuilder {
    public static Client createTestJerseyClient() {
        return createTestJerseyClient(Duration.seconds(10));
    }

    public static Client createTestJerseyClient(Duration timeout) {
        Environment environment = new Environment("test-dropwizard-apache-connector", RegisterApplication.customObjectMapper(),
                buildDefaultValidatorFactory().getValidator(), new MetricRegistry(),
                TestJerseyClientBuilder.class.getClassLoader());

        JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setTimeout(timeout);
        return new JerseyClientBuilder(environment).using(configuration).build("test-jersey-client");
    }

}
