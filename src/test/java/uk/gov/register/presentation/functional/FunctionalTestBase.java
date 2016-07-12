package uk.gov.register.presentation.functional;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.http.impl.conn.InMemoryDnsResolver;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import uk.gov.RegisterApplication;
import uk.gov.RegisterConfiguration;
import uk.gov.functional.WipeDatabaseRule;
import uk.gov.register.presentation.functional.testSupport.DBSupport;
import uk.gov.register.presentation.functional.testSupport.RegisterApplicationRunner;
import uk.gov.register.presentation.functional.testSupport.TestDAO;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FunctionalTestBase {
    public static final int APPLICATION_PORT = 9000;

    protected static Client client;

    protected static final TestDAO testDAO = TestDAO.get("ft_openregister_java", "postgres");
    protected static final DBSupport dbSupport = new DBSupport(testDAO);
    protected static Client authenticatingClient;
    @Rule
    public TestRule wipe = new WipeDatabaseRule();

    @ClassRule
    public static DropwizardAppRule<RegisterConfiguration> app = new DropwizardAppRule<>(RegisterApplication.class,
            ResourceHelpers.resourceFilePath("test-app-config.yaml"),
            ConfigOverride.config("database.url", RegisterApplicationRunner.DATABASE_URL),
            ConfigOverride.config("jerseyClient.timeout", "3000ms"));

    private static io.dropwizard.client.JerseyClientBuilder testClientBuilder() {
        InMemoryDnsResolver customDnsResolver = new InMemoryDnsResolver();
        customDnsResolver.add("address.beta.openregister.org", InetAddress.getLoopbackAddress());
        customDnsResolver.add("postcode.beta.openregister.org", InetAddress.getLoopbackAddress());
        customDnsResolver.add("localhost", InetAddress.getLoopbackAddress());
        return new io.dropwizard.client.JerseyClientBuilder(app.getEnvironment())
                .using(app.getConfiguration().getJerseyClientConfiguration())
                .using(customDnsResolver);
    }

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        client = testClientBuilder().withProperty("jersey.config.client.followRedirects",false).build("test client");

        authenticatingClient = testClientBuilder().build("authenticating client");
        authenticatingClient.register(HttpAuthenticationFeature.basicBuilder().credentials("foo", "bar").build());
    }

    Response getRequest(String path) {
        return getRequest("address", path);
    }

    Response getRequest(String registerName, String path) {
        String hostHeader = registerName + ".beta.openregister.org";
        try {
            Files.write(Paths.get("/tmp/test.log"), ("requesting " + path + " with Host header: " + hostHeader).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return client.target(String.format("http://%s:%d%s", hostHeader, app.getLocalPort(), path)).request().get();
    }

    protected void mintItems(String... items) {
        for (String item : items) {
            Response response = authenticatingClient.target(String.format("http://localhost:%d/load", app.getLocalPort())).request().header("Host", "address.beta.openregister.org")
                    .post(Entity.json(item));
            if (response.getStatus() > 399) {
                throw new RuntimeException("failed to mint items");
            }
        }
    }
}
