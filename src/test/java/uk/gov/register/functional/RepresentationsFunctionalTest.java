package uk.gov.register.functional;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import uk.gov.register.RegisterApplication;
import uk.gov.register.RegisterConfiguration;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class RepresentationsFunctionalTest {
    private final String extension;
    private final String expectedContentType;
    private final String expectedItemValue;
    private final String expectedEntryValue;
    private final String expectedRecordValue;
    private final String expectedRecordsValue;
    private final String expectedEntriesValue;
    private final String expectedRecordEntriesValue;

    private static Client client;
    private static Client authenticatingClient;

    @Rule
    public TestRule wipe = new WipeDatabaseRule();

    @ClassRule
    public static final DropwizardAppRule<RegisterConfiguration> appRule = new DropwizardAppRule<>(RegisterApplication.class,
            ResourceHelpers.resourceFilePath("test-app-config.yaml"),
            ConfigOverride.config("jerseyClient.timeout", "3000ms"),
            ConfigOverride.config("register", "register"));

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        JerseyClientBuilder clientBuilder = new JerseyClientBuilder(appRule.getEnvironment())
                .using(appRule.getConfiguration().getJerseyClientConfiguration());
        client = clientBuilder
                .build("test client");
        authenticatingClient = clientBuilder.build("authenticating client");
        authenticatingClient.register(HttpAuthenticationFeature.basicBuilder().credentials("foo", "bar").build());
    }

    @Before
    public void publishTestMessages() {
        Entity<String> entity = Entity.entity("add-item\t{\"fields\":[\"field1\"],\"register\":\"value1\",\"text\":\"The Entry 1\"}\n" +
                "add-item\t{\"fields\":[\"field1\",\"field2\"],\"register\":\"value2\",\"text\":\"The Entry 2\"}\n" +
                "append-entry\t2016-03-01T01:02:03Z\tsha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18\tvalue1\n" +
                "append-entry\t2016-03-02T02:03:04Z\tsha-256:63e5a0453b088e39265ca9f20fd03e2b206422e32989649adaca84426b531cd7\tvalue2\n" +
                "", ExtraMediaType.APPLICATION_RSF_TYPE);
        authenticatingClient.target(String.format("http://localhost:%d/load-rsf", appRule.getLocalPort()))
                .request()
                .post(entity);
    }

    private Response getRequest(String path) {
        return client.target(String.format("http://localhost:%d%s", appRule.getLocalPort(), path)).request().get();
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"csv", "text/csv;charset=UTF-8"},
                {"tsv", "text/tab-separated-values;charset=UTF-8"},
                {"ttl", "text/turtle;charset=UTF-8"},
                {"json", "application/json"},
                {"yaml", "text/yaml;charset=UTF-8"}
        });
    }

    public RepresentationsFunctionalTest(String extension, String expectedContentType) {
        this.extension = extension;
        this.expectedContentType = expectedContentType;
        this.expectedItemValue = fixture("fixtures/item." + extension);
        this.expectedEntryValue = fixture("fixtures/entry." + extension);
        this.expectedRecordValue = fixture("fixtures/record." + extension);
        this.expectedRecordsValue = fixture("fixtures/list." + extension);
        this.expectedEntriesValue = fixture("fixtures/entries." + extension);
        this.expectedRecordEntriesValue = fixture("fixtures/record-entries." + extension);
    }

    @Test
    public void representationIsSupportedForEntryResource() {
        assumeThat(expectedEntryValue, notNullValue());

        Response response = getRequest("/entry/1." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedEntryValue));
    }

    @Test
    public void representationIsSupportedForItemResource() {
        assumeThat(expectedItemValue, notNullValue());

        Response response = getRequest("/item/sha-256:877d8bd1ab71dc6e48f64b4ca83c6d7bf645a1eb56b34d50fa8a833e1101eb18." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedItemValue));
    }

    @Test
    public void representationIsSupportedForRecordResource() {
        assumeThat(expectedRecordValue, notNullValue());

        Response response = getRequest("/record/value1." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedRecordValue));
    }

    @Test
    public void representationIsSupportedForRecordsResource() {
        assumeThat(expectedRecordsValue, notNullValue());

        Response response = getRequest("/records." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedRecordsValue));
    }

    @Test
    public void representationIsSupportedForEntriesResource() {
        assumeThat(expectedEntriesValue, notNullValue());

        Response response = getRequest("/entries." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedEntriesValue));
    }

    @Test
    public void representationIsSupportedForRecordEntriesResource(){
        assumeThat(expectedRecordEntriesValue, notNullValue());

        Response response = getRequest("/record/value1/entries." + extension);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo(expectedContentType));
        assertThat(response.readEntity(String.class), equalTo(expectedRecordEntriesValue));
    }

    private static String fixture(String resourceName) {
        try {
            return Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }
}
