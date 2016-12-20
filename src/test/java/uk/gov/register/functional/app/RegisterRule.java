package uk.gov.register.functional.app;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
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
import java.util.List;

import static javax.ws.rs.client.Entity.entity;
import static uk.gov.register.views.representations.ExtraMediaType.APPLICATION_RSF_TYPE;

public class RegisterRule implements TestRule {
    private static final String REGISTER_DOMAIN = "test.register.gov.uk";
    private static final ImmutableMap<String, String> registerHostnames = ImmutableMap.of(
            "address", "address." + REGISTER_DOMAIN,
            "postcode", "postcode." + REGISTER_DOMAIN,
            "register", "register." + REGISTER_DOMAIN
    );
    private final WipeDatabaseRule wipeRule;
    private DropwizardAppRule<RegisterConfiguration> appRule;

    private TestRule wholeRule;

    private Client client;
    private List<Handle> handles = new ArrayList<>();

    public RegisterRule(ConfigOverride... overrides) {
        this.appRule = new DropwizardAppRule<>(RegisterApplication.class,
                ResourceHelpers.resourceFilePath("test-app-config.yaml"),
                overrides);
        ImmutableSet<String> registers = registerHostnames.keySet();
        wipeRule = new WipeDatabaseRule(registers.toArray(new String[registers.size()]));
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

    private WebTarget authenticatedTarget(String registerName) {
        WebTarget authenticatedTarget = targetRegister(registerName);
        authenticatedTarget.register(HttpAuthenticationFeature.basicBuilder().credentials("foo", "bar").build());
        return authenticatedTarget;
    }

    private WebTarget target(String targetHost) {
        return client.target("http://" + targetHost + ":" + appRule.getLocalPort());
    }

    /**
     * get a WebTarget pointing at a particular register.
     * if the registerName is recognized, the Host: will be set appropriately for that register
     * if not, it will default to a Host: of localhost (which will hit the default register)
     */
    public WebTarget targetRegister(String registerName) { // TODO: use RegisterName here
        return target(registerHostnames.getOrDefault(registerName, "localhost"));
    }

    public Response loadRsf(String registerName, String rsf) {
        return authenticatedTarget(registerName).path("/load-rsf")
                .request()
                .post(entity(rsf, APPLICATION_RSF_TYPE));
    }

    public Response mintLines(String registerName, String... payload) {
        return authenticatedTarget(registerName).path("/load")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(String.join("\n", payload)));
    }

    public Response getRequest(String registerName, String path) {
        return targetRegister(registerName).path(path).request().get();
    }

    public Response getRequest(String registerName, String path, String acceptedResponseType) {
        return targetRegister(registerName).path(path).request(acceptedResponseType).get();
    }

    public Response deleteRegisterData(String registerName) {
        return authenticatedTarget(registerName).path("/delete-register-data").request().delete();
    }

    public void wipe() {
        wipeRule.before();
    }

    private JerseyClientBuilder clientBuilder() {
        InMemoryDnsResolver customDnsResolver = new InMemoryDnsResolver();
        for (String registerHostname : registerHostnames.values()) {
            customDnsResolver.add(registerHostname, InetAddress.getLoopbackAddress());
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
    public Handle handleFor(String register) {
        Handle handle = new DBI(postgresConnectionString(register)).open();
        handles.add(handle);
        return handle;
    }

    private String postgresConnectionString(String register) {
        return String.format("jdbc:postgresql://localhost:5432/ft_openregister_java_%s?user=postgres&ApplicationName=RegisterRule", register);
    }
}
