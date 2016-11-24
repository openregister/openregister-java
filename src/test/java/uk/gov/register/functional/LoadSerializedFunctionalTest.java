package uk.gov.register.functional;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.http.impl.conn.InMemoryDnsResolver;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import uk.gov.register.RegisterApplication;
import uk.gov.register.RegisterConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.functional.db.TestDBItem;
import uk.gov.register.functional.db.TestRecord;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.register.functional.db.TestDBSupport.*;

public class LoadSerializedFunctionalTest {
    public static final int APPLICATION_PORT = 9000;
    private static Client client;
    private static final String registerName = "register";

    @Rule
    public TestRule wipe = new WipeDatabaseRule();

    @ClassRule
    public static final DropwizardAppRule<RegisterConfiguration> appRule = new DropwizardAppRule<>(RegisterApplication.class,
            ResourceHelpers.resourceFilePath("test-app-config.yaml"),
            ConfigOverride.config("database.url", postgresConnectionString),
            ConfigOverride.config("jerseyClient.timeout", "3000ms"),
            ConfigOverride.config("register", registerName));

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        client = testClient();
    }

    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-rsf.tsv")));
        Response r = send(input);
        assertThat(r.getStatus(), equalTo(204));

        TestDBItem storedItem = testItemDAO.getItems().get(0);
        assertThat(storedItem.contents.toString(), equalTo("{\"text\":\"SomeText\",\"register\":\"ft_openregister_test\"}"));
        assertThat(storedItem.hashValue.getValue(), equalTo("3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254"));

        Entry entry = testEntryDAO.getAllEntries().get(0);
        assertThat(entry.getEntryNumber(), is(1));
        assertThat(entry.getSha256hex().getValue(), is("3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254"));

        TestRecord record = testRecordDAO.getRecord("ft_openregister_test");
        assertThat(record.getEntryNumber(), equalTo(1));
        assertThat(record.getPrimaryKey(), equalTo("ft_openregister_test"));
    }

    @Test
    public void shouldReturnBadRequestWhenNotValidRsf() {
        String entry = "foo bar";
        Response response = send(entry);

        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("Error parsing : line must begin with legal command not: foo bar"));
    }

    @Test
    public void shouldReturnBadRequestForOrphanItems() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-orphan-rsf.tsv")));
        Response response = send(input);
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("{\"message\":\"no corresponding entry for item(s): \",\"orphanItems\":[{\"register\":\"ft_openregister_test\",\"text\":\"orphan item\"}]}"));
    }

    @Test
    public void shouldRollbackIfCheckedRootHashDoesNotMatchExpectedOne() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-rsf-invalid-root-hash.tsv")));

        Response r = send(input);

        assertThat(r.getStatus(), equalTo(409));
        assertThat(testItemDAO.getItems(), is(empty()));
        assertThat(testEntryDAO.getAllEntries(), is(empty()));
    }

    private Response send(String payload) {
        return authenticatingClient().target("http://localhost:" + APPLICATION_PORT + "/load-rsf")
                .request(ExtraMediaType.APPLICATION_RSF_TYPE)
                .post(Entity.entity(payload, ExtraMediaType.APPLICATION_RSF_TYPE));

    }

    private JerseyClient authenticatingClient() {
        ClientConfig configuration = new ClientConfig();
        configuration.register(HttpAuthenticationFeature.basic("foo", "bar"));
        return JerseyClientBuilder.createClient(configuration);
    }

    private static Client testClient() {
        InMemoryDnsResolver customDnsResolver = new InMemoryDnsResolver();
        customDnsResolver.add("address.beta.openregister.org", InetAddress.getLoopbackAddress());
        customDnsResolver.add("postcode.beta.openregister.org", InetAddress.getLoopbackAddress());
        customDnsResolver.add("register.beta.openregister.org", InetAddress.getLoopbackAddress());
        customDnsResolver.add("localhost", InetAddress.getLoopbackAddress());
        return new io.dropwizard.client.JerseyClientBuilder(appRule.getEnvironment())
                .using(appRule.getConfiguration().getJerseyClientConfiguration())
                .using(customDnsResolver)
                .build("test client");
    }

    Response getRequest(String path) {
        return client.target(String.format("http://localhost:%d%s", appRule.getLocalPort(), path)).request().get();
    }
}
