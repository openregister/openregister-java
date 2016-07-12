package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class RecordListResourceFunctionalTest extends FunctionalTestBase {
    @Before
    public void publishTestMessages() {
        mintItems(
                "{\"street\":\"ellis\",\"address\":\"12345\"}",
                "{\"street\":\"presley\",\"address\":\"6789\"}",
                "{\"street\":\"ellis\",\"address\":\"145678\"}",
                "{\"street\":\"updatedEllisName\",\"address\":\"145678\"}",
                "{\"street\":\"ellis\",\"address\":\"6789\"}"
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void newRecords_shouldReturnAllCurrentVersionsOnly() throws Exception {
        Response response = getRequest("/records.json");

        Map<String, Map<String, String>> responseMap = response.readEntity(Map.class);

        assertThat(responseMap.size(), equalTo(3));

        assertThat(responseMap.get("6789"), equalTo(ImmutableMap.of("entry-number", "5", "item-hash", "sha-256:" + DigestUtils.sha256Hex("{\"address\":\"6789\",\"street\":\"ellis\"}"), "entry-timestamp", responseMap.get("6789").get("entry-timestamp"), "street", "ellis", "address", "6789")));
        assertThat(responseMap.get("145678"), equalTo(ImmutableMap.of("entry-number", "4", "item-hash", "sha-256:" + DigestUtils.sha256Hex("{\"address\":\"145678\",\"street\":\"updatedEllisName\"}"), "entry-timestamp", responseMap.get("145678").get("entry-timestamp"), "street", "updatedEllisName", "address", "145678")));
        assertThat(responseMap.get("12345"), equalTo(ImmutableMap.of("entry-number", "1", "item-hash", "sha-256:" + DigestUtils.sha256Hex("{\"address\":\"12345\",\"street\":\"ellis\"}"), "entry-timestamp", responseMap.get("12345").get("entry-timestamp"), "street", "ellis", "address", "12345")));
    }

    @Test
    public void newRecords_setsAppropriateFilenameForDownload() {
        Response response = getRequest("address", "/records.json");
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION), containsString("filename=\"address-records.json\""));
    }

    @Test
    public void newRecords_hasLinkHeaderForNextAndPreviousPage() {
        Response response = getRequest("/records.json?page-index=1&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=2&page-size=1>; rel=\"next\""));

        response = getRequest("/records.json?page-index=2&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=3&page-size=1>; rel=\"next\",<?page-index=1&page-size=1>; rel=\"previous\""));

        response = getRequest("/records.json?page-index=3&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=2&page-size=1>; rel=\"previous\""));
    }

    @Test
    public void newRecordsPageHasXhtmlLangAttributes() {
        Response response = getRequest("address", "/records");

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Elements htmlElement = doc.select("html");
        assertThat(htmlElement.size(), equalTo(1));
        assertThat(htmlElement.first().attr("lang"), equalTo("en"));
        assertThat(htmlElement.first().attr("xml:lang"), equalTo("en"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchAllRecordsForAKeyValueCombinatiion() throws JSONException {
        Response response = getRequest("/records/street/ellis.json");
        Map<String, Map<String, String>> responseMap = response.readEntity(Map.class);

        assertThat(responseMap.size(), equalTo(2));

        assertThat(responseMap.get("6789"), equalTo(ImmutableMap.of("entry-number", "5", "item-hash", "sha-256:" + DigestUtils.sha256Hex("{\"address\":\"6789\",\"street\":\"ellis\"}"), "entry-timestamp", responseMap.get("6789").get("entry-timestamp"), "street", "ellis", "address", "6789")));
        assertThat(responseMap.get("12345"), equalTo(ImmutableMap.of("entry-number", "1", "item-hash", "sha-256:" + DigestUtils.sha256Hex("{\"address\":\"12345\",\"street\":\"ellis\"}"), "entry-timestamp", responseMap.get("12345").get("entry-timestamp"), "street", "ellis", "address", "12345")));
    }


    //Note: tests below will be removed once the old resources are deleted
    @Test
    public void oldFacetedResourceRedirectsToNewResource(){
        Response response = getRequest("/street/ellis.json");
        assertThat(response.getStatus(), equalTo(301));
        String expectedRedirect = "http://address.beta.openregister.org:" + app.getLocalPort() + "/records/street/ellis";
        assertThat(response.getHeaders().get("Location").get(0), equalTo(expectedRedirect));
    }
}

