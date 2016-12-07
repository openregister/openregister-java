package uk.gov.register.functional.app;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
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
import static uk.gov.register.functional.db.TestDBSupport.testEntryDAO;
import static uk.gov.register.functional.db.TestDBSupport.testItemDAO;
import static uk.gov.register.functional.db.TestDBSupport.testRecordDAO;
import static uk.gov.register.views.representations.ExtraMediaType.APPLICATION_RSF_TYPE;

public class RegisterRule implements TestRule {
    private DropwizardAppRule<RegisterConfiguration> appRule;

    private WebTarget authenticatedTarget;
    private Client client;
    public RegisterRule(String register, ConfigOverride... overrides) {
        ConfigOverride[] configOverrides = constructOverrides(register, overrides);
        this.appRule = new DropwizardAppRule<>(RegisterApplication.class,
                ResourceHelpers.resourceFilePath("test-app-config.yaml"),
                configOverrides);
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
                beforeTest();
                base.evaluate();
            }
        };
        return appRule.apply(me, description);
    }

    /*
     * executed before each test, but after appRule has set up the dropwizard app
     */
    private void beforeTest() {
        client = clientBuilder().build("test client");
        authenticatedTarget = client.target("http://localhost:" + appRule.getLocalPort());
        authenticatedTarget.register(HttpAuthenticationFeature.basicBuilder().credentials("foo", "bar").build());

        migrateDb();
        wipe();
    }

    private void migrateDb() {
        RegisterConfiguration configuration = appRule.getConfiguration();
        Flyway flyway = configuration.getFlywayFactory().build(configuration.getDatabase().build(appRule.getEnvironment().metrics(), "flyway_db"));
        flyway.migrate();
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
        testEntryDAO.wipeData();
        testItemDAO.wipeData();
        testRecordDAO.wipeData();
    }

    private JerseyClientBuilder clientBuilder() {
        return new JerseyClientBuilder(appRule.getEnvironment())
                .using(appRule.getConfiguration().getJerseyClientConfiguration());
    }
}
