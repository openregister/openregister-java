package uk.gov.register.functional.app;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import uk.gov.register.RegisterApplication;
import uk.gov.register.RegisterConfiguration;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.entity;

public class RegisterRule implements TestRule {
    private final WipeDatabaseRule wipeRule;
    private Client client;
    private Client authenticatingClient;
    private DropwizardAppRule<RegisterConfiguration> appRule;

    private TestRule wholeRule;

    public RegisterRule(String register) {
        this.appRule = new DropwizardAppRule<>(RegisterApplication.class,
                ResourceHelpers.resourceFilePath("test-app-config.yaml"),
                ConfigOverride.config("register", register));
        wipeRule = new WipeDatabaseRule();
        wholeRule = RuleChain
                .outerRule(appRule)
                .around(wipeRule);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        Statement me = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                client = clientBuilder().build("test client");
                authenticatingClient = clientBuilder().build("authenticating client");
                authenticatingClient.register(HttpAuthenticationFeature.basicBuilder().credentials("foo", "bar").build());
                base.evaluate();
            }
        };
        return wholeRule.apply(me, description);
    }

    public void loadRsf(String rsf) {
        authenticatingClient.target(String.format("http://localhost:%d/load-rsf", appRule.getLocalPort()))
                .request()
                .post(entity(rsf, ExtraMediaType.APPLICATION_RSF_TYPE));
    }

    public Response getRequest(String path) {
        return client.target(String.format("http://localhost:%d%s", appRule.getLocalPort(), path)).request().get();
    }

    public void wipe() {
        wipeRule.before();
    }

    private JerseyClientBuilder clientBuilder() {
        return new JerseyClientBuilder(appRule.getEnvironment())
                .using(appRule.getConfiguration().getJerseyClientConfiguration());
    }
}
