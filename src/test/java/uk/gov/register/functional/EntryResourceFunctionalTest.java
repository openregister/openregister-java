package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.jackson.Jackson;
import org.apache.commons.codec.digest.DigestUtils;
import org.hamcrest.Matcher;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertTrue;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.views.representations.ExtraMediaType.TEXT_HTML;

public class EntryResourceFunctionalTest {
    @ClassRule
    public static RegisterRule register = new RegisterRule();
    private final String item1 = "{\"address\":\"6789\",\"street\":\"elvis\"}";
    private final String item2 = "{\"address\":\"6790\",\"street\":\"presley\"}";
    private final String item1Hash = "sha-256:" + DigestUtils.sha256Hex(item1);
    private final String item2Hash = "sha-256:" + DigestUtils.sha256Hex(item2);
    private final WebTarget addressTarget = register.target(address);

    @Before
    public void publishTestMessages() throws Throwable {
        register.wipe();
        register.mintLines(address, item1, item2);
    }

    @Test
    public void getEntriesView_itemHashesAreRenderedAsLinks() {
        Response response = register.getRequest(address, "/entries", TEXT_HTML);

        assertThat(response.getStatus(), equalTo(200));

        Document doc = Jsoup.parse(response.readEntity(String.class));
        String entry1Text = doc.getElementsByTag("table").select("a[href=/item/" + item1Hash + "]").first().text();
        String entry2Text = doc.getElementsByTag("table").select("a[href=/item/" + item2Hash + "]").first().text();

        assertThat(entry1Text, equalTo(item1Hash));
        assertThat(entry2Text, equalTo(item2Hash));
    }

    @Test
    public void getEntries_return400ResponseWhenStartIsNotANumber() {
        Response response = addressTarget.path("/entries").queryParam("start", "not-a-number").request().get();

        assertThat(response.getMediaType().getType(), equalTo("text"));
        assertThat(response.getMediaType().getSubtype(), equalTo("html"));
        assertThat(response.getStatus(), equalTo(400));
    }

    @Test
    public void getEntries_return400ResponseWhenLimitIsNotANumber() {
        Response response = addressTarget.path("/entries").queryParam("limit", "not-a-number").request().get();

        assertThat(response.getMediaType().getType(), equalTo("text"));
        assertThat(response.getMediaType().getSubtype(), equalTo("html"));
        assertThat(response.getStatus(), equalTo(400));
    }

    @Test
    public void getEntriesAsJson() throws JSONException, IOException {
        Response response = register.getRequest(address, "/entries.json");

        assertThat(response.getStatus(), equalTo(200));

        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);
        assertThat(res.isArray(), equalTo(true));
        assertEntryInJsonNode(res.get(0), 1, item1Hash);
        assertEntryInJsonNode(res.get(1), 2, item2Hash);
    }

    @Test
    public void getEntryByEntryNumber() throws JSONException, IOException {
        Response response = register.getRequest(address, "/entry/1.json");

        assertThat(response.getStatus(), equalTo(200));
        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);
        assertEntryInJsonNode(res, 1, item1Hash);
    }

    @Test
    public void entryView_itemHashIsRenderedAsALink() {
        Response response = register.getRequest(address, "/entry/1", TEXT_HTML);

        Document doc = Jsoup.parse(response.readEntity(String.class));
        String text = doc.getElementsByTag("table").select("a[href=/item/" + item1Hash + "]").first().text();
        assertThat(text, equalTo(item1Hash));
    }

    @Test
    public void return200ResponseForTextHtmlMediaTypeWhenItemExists() {
        assertThat(register.getRequest(address, "/entry/1", MediaType.TEXT_HTML).getStatus(), equalTo(200));
    }

    @Test
    public void return404ResponseWhenEntryNotExist() {
        assertThat(register.getRequest(address, "/entry/5001").getStatus(), equalTo(404));
    }

    @Test
    public void return404ResponseWhenEntryNumberIsNotAnIntegerValue() {
        assertThat(register.getRequest(address, "/entry/a2").getStatus(), equalTo(404));
    }

    @Test
    public void entryResource_retrievesTimestampsInUTC() throws IOException {
        Instant now = Instant.now();

        Response response = register.getRequest(address, "/entry/1.json");
        Map<String, String> responseData = response.readEntity(Map.class);
        Instant entryTimestamp = Instant.parse(responseData.get("entry-timestamp"));

        assertThat(entryTimestamp, between(now.minus(30, SECONDS), now.plus(30, SECONDS)));
    }

    private void assertEntryInJsonNode(JsonNode actualJsonNode, int expectedEntryNumber, String expectedHash) {
        assertThat(actualJsonNode.get("index-entry-number").asInt(), equalTo(expectedEntryNumber));
        assertThat(actualJsonNode.get("entry-number").asInt(), equalTo(expectedEntryNumber));
        assertThat(actualJsonNode.get("item-hash").get(0).asText(), equalTo(expectedHash));
        assertTrue(actualJsonNode.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));
    }

    private Matcher<Instant> between(Instant lower, Instant upper) {
        return allOf(greaterThan(lower), lessThan(upper));
    }
}
