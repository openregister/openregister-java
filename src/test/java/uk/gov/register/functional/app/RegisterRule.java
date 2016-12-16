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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.entity;
import static uk.gov.register.views.representations.ExtraMediaType.APPLICATION_RSF_TYPE;

public class RegisterRule implements TestRule {
    private final WipeDatabaseRule wipeRule;
    private DropwizardAppRule<RegisterConfiguration> appRule;

    private TestRule wholeRule;

    private WebTarget authenticatedTarget;
    private Client client;
    public RegisterRule(String register, ConfigOverride... overrides) {
        ConfigOverride[] configOverrides = constructOverrides(register, overrides);
        this.appRule = new DropwizardAppRule<>(RegisterApplication.class,
                ResourceHelpers.resourceFilePath("test-app-config.yaml"),
                configOverrides);
        wipeRule = new WipeDatabaseRule();
        wholeRule = RuleChain
                .outerRule(appRule)
                .around(wipeRule);
    }

    private ConfigOverride[] constructOverrides(String register, ConfigOverride[] overrides) {
        ConfigOverride[] allOverrides = new ConfigOverride[overrides.length+1];
        allOverrides[0] = ConfigOverride.config("register", register);
        System.arraycopy(overrides, 0, allOverrides, 1, overrides.length);
        return allOverrides;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        Statement me = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                client = clientBuilder().build("test client");
                authenticatedTarget = client.target("http://localhost:" + appRule.getLocalPort());
                authenticatedTarget.register(HttpAuthenticationFeature.basicBuilder().credentials("foo", "bar").build());
                base.evaluate();
            }
        };
        return wholeRule.apply(me, description);
    }

    public WebTarget target() {
        return client.target("http://localhost:" + appRule.getLocalPort());
    }

    public Response loadRsf(String rsf) {
        Response post = authenticatedTarget.path("/load-rsf")
                .request()
                .post(entity(rsf, APPLICATION_RSF_TYPE));
        if (post.getStatus() >= 400) {
            throw new RuntimeException("Failed to load RSF, got response " + post);
        }
        return post;
    }

    public Response mintLines(String... payload) {
        return authenticatedTarget.path("/load")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(String.join("\n", payload)));
    }

    public Response getRequest(String path) {
        return target().path(path).request().get();
    }

    public Response getRequest(String path, String acceptedResponseType) {
        return target().path(path).request(acceptedResponseType).get();
    }

    public Response deleteRegisterData() {
        return authenticatedTarget.path("/delete-register-data").request().delete();
    }

    public void wipe() {
        wipeRule.before();
    }

    private JerseyClientBuilder clientBuilder() {
        return new JerseyClientBuilder(appRule.getEnvironment())
                .using(appRule.getConfiguration().getJerseyClientConfiguration());
    }
}
