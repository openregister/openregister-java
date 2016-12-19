package uk.gov.register.functional;

import io.dropwizard.testing.ConfigOverride;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.db.TestEntry;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static uk.gov.register.views.representations.ExtraMediaType.TEXT_HTML;

@RunWith(Parameterized.class)
public class AnalyticsFunctionalTest {

    private static final String TRACKING_ID_NOT_PRESENT = null;
    private static final String TRACKING_ID_EMPTY = "";
    private static final String TRACKING_ID_VALID = "UA-12345678-1";

    private static final TestEntry testEntry1;
    private static final TestEntry testEntry2;
    private static final String testEntry1Key = "st1";
    private static final String testEntry2Key = "st2";

    static {
        testEntry1 = TestEntry.anEntry(1, "{\"street\":\"" + testEntry1Key + "\",\"address\":\"12345\"}", "12345");
        testEntry2 = TestEntry.anEntry(2, "{\"street\":\"" + testEntry2Key + "\",\"address\":\"12346\"}", "12346");
    }

    @Rule
    public RegisterRule register;

    @Before
    public void setup() {
        register.wipe();
        register.mintLines("address",
                testEntry1.itemJson,
                testEntry2.itemJson);
    }

    private final String targetUrl;
    private final Boolean shouldIncludeAnalytics;

    public AnalyticsFunctionalTest(String targetUrl, String trackingId, Boolean shouldIncludeAnalytics) {
        this.register = createRegister(trackingId);
        this.targetUrl = targetUrl;
        this.shouldIncludeAnalytics = shouldIncludeAnalytics;
    }

    @Parameterized.Parameters(name = "{index} - url: {0} with code: {1} should include analytics: {2}")
    public static List data() {
        List sourceData = new ArrayList();

        sourceData.addAll(generateTestSetFor("/"));
        sourceData.addAll(generateTestSetFor("/download"));
        sourceData.addAll(generateTestSetFor("/entries"));
        sourceData.addAll(generateTestSetFor("/entry/9999999999"));
        sourceData.addAll(generateTestSetFor("/entry/1"));
        sourceData.addAll(generateTestSetFor("/entry/2"));

        sourceData.addAll(generateTestSetFor("/records"));
        sourceData.addAll(generateTestSetFor("/records/non-existent-record"));
        sourceData.addAll(generateTestSetFor("/records/non-existent-record/entries"));
        sourceData.addAll(generateTestSetFor("/records/" + testEntry1Key));
        sourceData.addAll(generateTestSetFor("/records/" + testEntry2Key));
        sourceData.addAll(generateTestSetFor("/records/" + testEntry1Key + "/entries"));
        sourceData.addAll(generateTestSetFor("/records/" + testEntry2Key + "/entries"));

        sourceData.addAll(generateTestSetFor("/item/sha-256:non-existent-item"));
        sourceData.addAll(generateTestSetFor("/item/" + testEntry1.sha256hex));
        sourceData.addAll(generateTestSetFor("/item/" + testEntry2.sha256hex));

        sourceData.addAll(generateTestSetFor("/not-found-page"));

        return sourceData;
    }

    @Test
    public void checkAnalyticsScriptsPresence() throws Exception {
        Response response = register.getRequest("address", targetUrl, TEXT_HTML);

        Document doc = Jsoup.parse(response.readEntity(String.class));
        assertThat(response.getStatus(), lessThan(500));

        Boolean docIncludesAnalyticsId = doc.getElementById("analytics-tracking-id") != null;
        Boolean docIncludesMainAnalytics = doc.getElementById("analytics-main") != null;
        Boolean docIncludesExtLinksAnalytics = doc.getElementById("analytics-external-links") != null;

        assertThat(docIncludesAnalyticsId, equalTo(shouldIncludeAnalytics));
        assertThat(docIncludesMainAnalytics, equalTo(shouldIncludeAnalytics));
        assertThat(docIncludesExtLinksAnalytics, equalTo(shouldIncludeAnalytics));
    }

    private static RegisterRule createRegister(String trackingId) {
        if (trackingId != null) {
            return new RegisterRule("address", ConfigOverride.config("trackingId", trackingId));
        }
        return new RegisterRule("address");
    }

    private static List<Object> generateTestSetFor(String url) {
        return Arrays.asList(
                new Object[]{url, TRACKING_ID_NOT_PRESENT, false},
                new Object[]{url, TRACKING_ID_EMPTY, false},
                new Object[]{url, TRACKING_ID_VALID, true}
        );
    }

}
