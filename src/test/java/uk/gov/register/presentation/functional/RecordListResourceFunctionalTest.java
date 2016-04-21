package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class RecordListResourceFunctionalTest extends FunctionalTestBase {
    @Before
    public void publishTestMessages() {
        dbSupport.publishMessages(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}",
                "{\"hash\":\"hash4\",\"entry\":{\"name\":\"updatedEllisName\",\"address\":\"145678\"}}",
                "{\"hash\":\"hash5\",\"entry\":{\"name\":\"ellis\",\"address\":\"6789\"}}"
        ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void newRecords_shouldReturnAllCurrentVersionsOnly() throws Exception {
        Response response = getRequest("/_records.json");

        Map<String, Map<String, String>> responseMap = response.readEntity(Map.class);

        assertThat(responseMap.size(), equalTo(3));

        assertThat(responseMap.get("6789"), equalTo(ImmutableMap.of("entry-number", "5", "item-hash", "sha-256:" + DigestUtils.sha256Hex("{\"name\":\"ellis\",\"address\":\"6789\"}"), "entry-timestamp", responseMap.get("6789").get("entry-timestamp"), "name", "ellis", "address", "6789")));
        assertThat(responseMap.get("145678"), equalTo(ImmutableMap.of("entry-number", "4", "item-hash", "sha-256:" + DigestUtils.sha256Hex("{\"name\":\"updatedEllisName\",\"address\":\"145678\"}"), "entry-timestamp", responseMap.get("145678").get("entry-timestamp"), "name", "updatedEllisName", "address", "145678")));
        assertThat(responseMap.get("12345"), equalTo(ImmutableMap.of("entry-number", "1", "item-hash", "sha-256:" + DigestUtils.sha256Hex("{\"name\":\"ellis\",\"address\":\"12345\"}"), "entry-timestamp", responseMap.get("12345").get("entry-timestamp"), "name", "ellis", "address", "12345")));
    }

    @Test
    public void newRecords_setsAppropriateFilenameForDownload() {
        Response response = getRequest("address", "/_records.json");
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION), containsString("filename=\"address-newrecords.json\""));
    }

    @Test
    public void newRecords_hasLinkHeaderForNextAndPreviousPage() {
        Response response = getRequest("/_records.json?page-index=1&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=2&page-size=1>; rel=\"next\""));

        response = getRequest("/_records.json?page-index=2&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=3&page-size=1>; rel=\"next\",<?page-index=1&page-size=1>; rel=\"previous\""));

        response = getRequest("/_records.json?page-index=3&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=2&page-size=1>; rel=\"previous\""));
    }

    @Test
    public void newRecordsPageHasXhtmlLangAttributes() {
        Response response = getRequest("address", "/_records");

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Elements htmlElement = doc.select("html");
        assertThat(htmlElement.size(), equalTo(1));
        assertThat(htmlElement.first().attr("lang"), equalTo("en"));
        assertThat(htmlElement.first().attr("xml:lang"), equalTo("en"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchAllRecordsForAKeyValueCombinatiion() throws JSONException {
        Response response = getRequest("/records/name/ellis.json");
        Map<String, Map<String, String>> responseMap = response.readEntity(Map.class);

        assertThat(responseMap.size(), equalTo(2));

        assertThat(responseMap.get("6789"), equalTo(ImmutableMap.of("entry-number", "5", "item-hash", "sha-256:" + DigestUtils.sha256Hex("{\"name\":\"ellis\",\"address\":\"6789\"}"), "entry-timestamp", responseMap.get("6789").get("entry-timestamp"), "name", "ellis", "address", "6789")));
        assertThat(responseMap.get("12345"), equalTo(ImmutableMap.of("entry-number", "1", "item-hash", "sha-256:" + DigestUtils.sha256Hex("{\"name\":\"ellis\",\"address\":\"12345\"}"), "entry-timestamp", responseMap.get("12345").get("entry-timestamp"), "name", "ellis", "address", "12345")));
    }


    //Note: tests below will be removed once the old resources are deleted
    @Test
    public void oldFacetedResourceRedirectsToNewResource(){
        Response response = getRequest("/name/ellis.json");
        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaders().get("Location").get(0), equalTo("http://address.beta.openregister.org/records/name/ellis"));
    }

    @Test
    public void oldRecords_shouldReturnAllCurrentVersionsOnly() throws Exception {
        Response response = getRequest("/records.json");

        String jsonResponse = response.readEntity(String.class);
        JSONAssert.assertEquals(jsonResponse,
                "[" +
                        "{\"serial-number\":5,\"hash\":\"hash5\",\"entry\":{\"name\":\"ellis\",\"address\":\"6789\"}}," +
                        "{\"serial-number\":4,\"hash\":\"hash4\",\"entry\":{\"name\":\"updatedEllisName\",\"address\":\"145678\"}}," +
                        "{\"serial-number\":1,\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}" +
                        "]"
                , false);
    }

    @Test
    public void oldRecords_setsAppropriateFilenameForDownload() {
        Response response = getRequest("address", "/records.json");
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION), containsString("filename=\"address-records.json\""));
    }

    @Test
    public void oldRecords_hasLinkHeaderForNextAndPreviousPage() {
        Response response = getRequest("/records.json?page-index=1&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=2&page-size=1>; rel=\"next\""));

        response = getRequest("/records.json?page-index=2&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=3&page-size=1>; rel=\"next\",<?page-index=1&page-size=1>; rel=\"previous\""));

        response = getRequest("/records.json?page-index=3&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("<?page-index=2&page-size=1>; rel=\"previous\""));
    }

    @Test
    public void oldRecordsPageHasXhtmlLangAttributes() {
        Response response = getRequest("address", "/records");

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Elements htmlElement = doc.select("html");
        assertThat(htmlElement.size(), equalTo(1));
        assertThat(htmlElement.first().attr("lang"), equalTo("en"));
        assertThat(htmlElement.first().attr("xml:lang"), equalTo("en"));
    }

}

