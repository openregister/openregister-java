package uk.gov.organisation.client;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;

import javax.ws.rs.client.Client;

import static javax.validation.Validation.buildDefaultValidatorFactory;

public abstract class TestJerseyClientBuilder {
    public static Client createTestJerseyClient() {
        return createTestJerseyClient(Duration.seconds(10));
    }

    public static Client createTestJerseyClient(Duration timeout) {
        Environment environment = new Environment("test-dropwizard-apache-connector", Jackson.newObjectMapper(),
                buildDefaultValidatorFactory().getValidator(), new MetricRegistry(),
                TestJerseyClientBuilder.class.getClassLoader());

        JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setTimeout(timeout);
        return new JerseyClientBuilder(environment).using(configuration).build("test-jersey-client");
    }

}
