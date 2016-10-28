package uk.gov.register.functional;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.register.RegisterApplication;
import uk.gov.register.RegisterConfiguration;
import uk.gov.register.functional.app.WipeDatabaseRule;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static uk.gov.register.functional.db.TestDBSupport.postgresConnectionString;

@RunWith(Parameterized.class)
public class AnalyticsFunctionalTest {

    private static final String TRACKING_ID_NOT_PRESENT = null;
    private static final String TRACKING_ID_EMPTY = "";
    private static final String TRACKING_ID_VALID = "UA-12345678-1";

    @Rule
    public TestRule wipe = new WipeDatabaseRule();

    @Rule
    public DropwizardAppRule<RegisterConfiguration> appRule;

    private final String targetUrl;
    private final String trackingId;
    private final Boolean shouldIncludeAnalytics;


    public AnalyticsFunctionalTest(String targetUrl, String trackingId, Boolean shouldIncludeAnalytics) {
        this.appRule = createAppRule(trackingId);
        this.targetUrl = targetUrl;
        this.trackingId = trackingId;
        this.shouldIncludeAnalytics = shouldIncludeAnalytics;
    }

    @Parameterized.Parameters(name = "{index} - url: {0} with code: {1} should include analytics: {2}")
    public static List data() {
        List sourceData = new ArrayList();

        sourceData.addAll(generateTestSetFor("/"));
        sourceData.addAll(generateTestSetFor("/download"));
        sourceData.addAll(generateTestSetFor("/entries"));
        sourceData.addAll(generateTestSetFor("/records"));
        sourceData.addAll(generateTestSetFor("/not-found-page"));

        return sourceData;
    }

    @Test
    public void checkAnalyticsScriptsPresence() throws Exception {
        Client client = getTestClient(this.appRule);
        Response response = client.target(String.format("http://localhost:%d%s", this.appRule.getLocalPort(), targetUrl))
                .request().get();

        Document doc = Jsoup.parse(response.readEntity(String.class));
        assertThat(response.getStatus(), lessThan(500));

        Element trackingIdElem = doc.getElementById("analytics-tracking-id");
        Boolean docIncludesAnalyticsId = trackingIdElem != null;
        Boolean docIncludesMainAnalytics = doc.getElementById("analytics-main") != null;
        Boolean docIncludesExtLinksAnalytics = doc.getElementById("analytics-external-links") != null;

        assertThat(docIncludesAnalyticsId, equalTo(shouldIncludeAnalytics));
        assertThat(docIncludesMainAnalytics, equalTo(shouldIncludeAnalytics));
        assertThat(docIncludesExtLinksAnalytics, equalTo(shouldIncludeAnalytics));

        if (shouldIncludeAnalytics) {
            assertThat(trackingIdElem.html(), containsString("var gaTrackingId = '" + trackingId + "';"));
        }
    }

    private Client getTestClient(DropwizardAppRule<RegisterConfiguration> appRule) {
        return new io.dropwizard.client.JerseyClientBuilder(appRule.getEnvironment())
                .using(appRule.getConfiguration().getJerseyClientConfiguration())
                .build("test client");
    }

    private static DropwizardAppRule<RegisterConfiguration> createAppRule(String trackingId) {
        ArrayList<ConfigOverride> configOverrides = new ArrayList<>(Arrays.asList(
                ConfigOverride.config("database.url", postgresConnectionString),
                ConfigOverride.config("jerseyClient.timeout", "3000ms"),
                ConfigOverride.config("register", "register")
        ));

        if (trackingId != null) {
            configOverrides.add(ConfigOverride.config("trackingId", trackingId));
        }

        return new DropwizardAppRule<>(RegisterApplication.class,
                ResourceHelpers.resourceFilePath("test-app-config.yaml"),
                configOverrides.toArray(new ConfigOverride[configOverrides.size()]));
    }

    private static List<Object> generateTestSetFor(String url) {
        return Arrays.asList(
                new Object[]{url, TRACKING_ID_NOT_PRESENT, false},
                new Object[]{url, TRACKING_ID_EMPTY, false},
                new Object[]{url, TRACKING_ID_VALID, true}
        );
    }
}
