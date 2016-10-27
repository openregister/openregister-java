package uk.gov.register.functional;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.register.RegisterApplication;
import uk.gov.register.RegisterConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.lang.reflect.Array;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.register.functional.db.TestDBSupport.postgresConnectionString;

@RunWith(Parameterized.class)
public class AnalyticsFunctionalTest {

    private static final String TRACKING_ID_NOT_PRESENT = null;
    private static final String TRACKING_ID_EMPTY = "";
    private static final String TRACKING_ID_VALID = "UA-12345678-1";

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
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][] {
                { "/", TRACKING_ID_NOT_PRESENT, false },
                { "/", TRACKING_ID_EMPTY, false },
                { "/", TRACKING_ID_VALID, true }
        });
    }

    @Test
    public void checkAnalyticsScriptsPresence() throws Exception {
        Client client = getTestClient(this.appRule);
        Response response = client.target(String.format("http://localhost:%d%s", this.appRule.getLocalPort(), targetUrl))
                .request().get();

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Boolean docInludesAnalyticsId = doc.getElementById("analytics-tracking-id") != null;
        Boolean docIncludesMainAnalytics = doc.getElementById("analytics-main") != null;
        Boolean docIncludesExtLinksAnalytics = doc.getElementById("analytics-external-links") != null;

        assertThat(docInludesAnalyticsId, equalTo(shouldIncludeAnalytics));
        assertThat(docIncludesMainAnalytics, equalTo(shouldIncludeAnalytics));
        assertThat(docIncludesExtLinksAnalytics, equalTo(shouldIncludeAnalytics));
    }

    private Client getTestClient(DropwizardAppRule<RegisterConfiguration> appRule) {
        return new io.dropwizard.client.JerseyClientBuilder(appRule.getEnvironment())
                .using(appRule.getConfiguration().getJerseyClientConfiguration())
                .build("test client");
    }

    private static DropwizardAppRule<RegisterConfiguration> createAppRule(String trackingId){
        ArrayList<ConfigOverride> configOverrides = new ArrayList<>(Arrays.asList(
                ConfigOverride.config("database.url", postgresConnectionString),
                ConfigOverride.config("jerseyClient.timeout", "3000ms"),
                ConfigOverride.config("register", "register")
        ));

        if(trackingId != null){
            configOverrides.add(ConfigOverride.config("trackingId", trackingId));
        }

        return new DropwizardAppRule<>(RegisterApplication.class,
                ResourceHelpers.resourceFilePath("test-app-config.yaml"),
                configOverrides.toArray(new ConfigOverride[configOverrides.size()]));

    }
}
