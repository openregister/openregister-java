package uk.gov.register.functional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.db.TestEntry;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static uk.gov.register.views.representations.ExtraMediaType.TEXT_HTML;

@RunWith(Parameterized.class)
public class AnalyticsFunctionalTest {

    private static final String REGISTER_WITH_MISSING_TRACKING_ID = "register";
    private static final String REGISTER_WITH_EMPTY_TRACKING_ID = "postcode";
    private static final String REGISTER_WITH_VALID_TRACKING_ID = "address";

    private static final TestEntry testEntry1;
    private static final TestEntry testEntry2;
    private static final String testEntry1Key = "st1";
    private static final String testEntry2Key = "st2";

    static {
        testEntry1 = TestEntry.anEntry(1, "{\"street\":\"" + testEntry1Key + "\",\"address\":\"12345\"}", "12345");
        testEntry2 = TestEntry.anEntry(2, "{\"street\":\"" + testEntry2Key + "\",\"address\":\"12346\"}", "12346");
    }

    @ClassRule
    public static RegisterRule register = new RegisterRule();

    @Before
    public void setup() {
        register.wipe();
        register.mintLines("address", testEntry1.itemJson, testEntry2.itemJson);
    }

    private final String targetUrl;

    public AnalyticsFunctionalTest(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @Parameterized.Parameters(name = "{index} - url: {0}")
    public static List data() {
        List<Object[]> sourceData = new ArrayList<>();

        sourceData.add(generateTestSetFor("/"));
        sourceData.add(generateTestSetFor("/download"));
        sourceData.add(generateTestSetFor("/entries"));
        sourceData.add(generateTestSetFor("/entry/9999999999"));
        sourceData.add(generateTestSetFor("/entry/1"));
        sourceData.add(generateTestSetFor("/entry/2"));
        sourceData.add(generateTestSetFor("/records"));
        sourceData.add(generateTestSetFor("/records/non-existent-record"));
        sourceData.add(generateTestSetFor("/records/non-existent-record/entries"));
        sourceData.add(generateTestSetFor("/records/" + testEntry1Key));
        sourceData.add(generateTestSetFor("/records/" + testEntry2Key));
        sourceData.add(generateTestSetFor("/records/" + testEntry1Key + "/entries"));
        sourceData.add(generateTestSetFor("/records/" + testEntry2Key + "/entries"));
        sourceData.add(generateTestSetFor("/item/sha-256:non-existent-item"));
        sourceData.add(generateTestSetFor("/item/" + testEntry1.sha256hex));
        sourceData.add(generateTestSetFor("/item/" + testEntry2.sha256hex));
        sourceData.add(generateTestSetFor("/not-found-page"));

        return sourceData;
    }

    @Test
    public void emptyTrackingId_shouldNotIncludeAnalyticsCode() throws Exception {
        Response response = register.getRequest(REGISTER_WITH_EMPTY_TRACKING_ID, targetUrl, TEXT_HTML);

        Document doc = Jsoup.parse(response.readEntity(String.class));
        assertThat(response.getStatus(), lessThan(500));

        Boolean docIncludesAnalyticsId = doc.getElementById("analytics-tracking-id") != null;
        Boolean docIncludesMainAnalytics = doc.getElementById("analytics-main") != null;
        Boolean docIncludesExtLinksAnalytics = doc.getElementById("analytics-external-links") != null;

        assertThat(docIncludesAnalyticsId, equalTo(false));
        assertThat(docIncludesMainAnalytics, equalTo(false));
        assertThat(docIncludesExtLinksAnalytics, equalTo(false));
    }

    @Test
    public void missingTrackingId_shouldNotIncludeAnalyticsCode() throws Exception {
        Response response = register.getRequest(REGISTER_WITH_MISSING_TRACKING_ID, targetUrl, TEXT_HTML);

        Document doc = Jsoup.parse(response.readEntity(String.class));
        assertThat(response.getStatus(), lessThan(500));

        Boolean docIncludesAnalyticsId = doc.getElementById("analytics-tracking-id") != null;
        Boolean docIncludesMainAnalytics = doc.getElementById("analytics-main") != null;
        Boolean docIncludesExtLinksAnalytics = doc.getElementById("analytics-external-links") != null;

        assertThat(docIncludesAnalyticsId, equalTo(false));
        assertThat(docIncludesMainAnalytics, equalTo(false));
        assertThat(docIncludesExtLinksAnalytics, equalTo(false));
    }

    @Test
    public void validTrackingId_shouldIncludeAnalyticsCode() throws Exception {
        Response response = register.getRequest(REGISTER_WITH_VALID_TRACKING_ID, targetUrl, TEXT_HTML);

        Document doc = Jsoup.parse(response.readEntity(String.class));
        assertThat(response.getStatus(), lessThan(500));

        Boolean docIncludesAnalyticsId = doc.getElementById("analytics-tracking-id") != null;
        Boolean docIncludesMainAnalytics = doc.getElementById("analytics-main") != null;
        Boolean docIncludesExtLinksAnalytics = doc.getElementById("analytics-external-links") != null;

        assertThat(docIncludesAnalyticsId, equalTo(true));
        assertThat(docIncludesMainAnalytics, equalTo(true));
        assertThat(docIncludesExtLinksAnalytics, equalTo(true));
    }

    private static Object[] generateTestSetFor(String url) {
        return new Object[]{url};
    }
}
