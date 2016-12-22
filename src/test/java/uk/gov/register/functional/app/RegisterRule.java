package uk.gov.register.functional.app;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.http.impl.conn.InMemoryDnsResolver;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.RegisterApplication;
import uk.gov.register.RegisterConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.ws.rs.client.Entity.entity;
import static uk.gov.register.views.representations.ExtraMediaType.APPLICATION_RSF_TYPE;

public class RegisterRule implements TestRule {
    private final WipeDatabaseRule wipeRule;
    private DropwizardAppRule<RegisterConfiguration> appRule;

    private TestRule wholeRule;

    private Client client;
    private List<Handle> handles = new ArrayList<>();

    public RegisterRule() {
        this.appRule = new DropwizardAppRule<>(RegisterApplication.class,
                ResourceHelpers.resourceFilePath("test-app-config.yaml"));
        String[] registers = Arrays.stream(TestRegister.values())
                .map(TestRegister::name)
                .toArray(String[]::new);
        wipeRule = new WipeDatabaseRule(registers);
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
                base.evaluate();
                handles.forEach(Handle::close);
            }
        };
        return wholeRule.apply(me, description);
    }

    private WebTarget authenticatedTarget(TestRegister register) {
        return target(register).register(HttpAuthenticationFeature.basicBuilder().credentials("foo", "bar").build());
    }

    private WebTarget target(String targetHost) {
        return client.target("http://" + targetHost + ":" + appRule.getLocalPort());
    }

    public WebTarget target(TestRegister register) {
        return target(register.getHostname());
    }

    public Response loadRsf(TestRegister register, String rsf) {
        return authenticatedTarget(register).path("/load-rsf")
                .request()
                .post(entity(rsf, APPLICATION_RSF_TYPE));
    }

    public Response mintLines(TestRegister register, String... payload) {
        return authenticatedTarget(register).path("/load")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(String.join("\n", payload)));
    }

    public Response getRequest(TestRegister register, String path) {
        return target(register).path(path).request().get();
    }

    public Response getRequest(TestRegister register, String path, String acceptedResponseType) {
        return target(register).path(path).request(acceptedResponseType).get();
    }

    public Response deleteRegisterData(TestRegister register) {
        return authenticatedTarget(register).path("/delete-register-data").request().delete();
    }

    public void wipe() {
        wipeRule.before();
    }

    private JerseyClientBuilder clientBuilder() {
        InMemoryDnsResolver customDnsResolver = new InMemoryDnsResolver();
        for (TestRegister register : TestRegister.values()) {
            customDnsResolver.add(register.getHostname(), InetAddress.getLoopbackAddress());
        }
        customDnsResolver.add("localhost", InetAddress.getLoopbackAddress());
        return new JerseyClientBuilder(appRule.getEnvironment())
                .using(appRule.getConfiguration().getJerseyClientConfiguration())
                .using(customDnsResolver);
    }

    /**
     * provides a DBI Handle for the given register
     * the handle will automatically be closed by the RegisterRule
     */
    public Handle handleFor(TestRegister register) {
        Handle handle = new DBI(postgresConnectionString(register)).open();
        handles.add(handle);
        return handle;
    }

    private String postgresConnectionString(TestRegister register) {
        return String.format("jdbc:postgresql://localhost:5432/ft_openregister_java_%s?user=postgres&ApplicationName=RegisterRule", register.name());
    }
}
