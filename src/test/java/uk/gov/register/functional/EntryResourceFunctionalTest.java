package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.jackson.Jackson;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class EntryResourceFunctionalTest extends FunctionalTestBase {
    private final String item1 = "{\"address\":\"6789\",\"street\":\"elvis\"}";
    private final String item2 = "{\"address\":\"6790\",\"street\":\"presley\"}";
    private final String item1Hash = "sha-256:" + DigestUtils.sha256Hex(item1);
    private final String item2Hash = "sha-256:" + DigestUtils.sha256Hex(item2);


    @Before
    public void publishTestMessages() throws Throwable {
        mintItems(item1, item2);
    }

    @Test
    public void getEntriesView_itemHashesAreRenderedAsLinks() {
        Response response = getRequest("/entries");

        assertThat(response.getStatus(), equalTo(200));

        Document doc = Jsoup.parse(response.readEntity(String.class));
        String entry1Text = doc.getElementsByTag("table").select("a[href=/item/"+ item1Hash + "]").first().text();
        String entry2Text = doc.getElementsByTag("table").select("a[href=/item/"+ item2Hash + "]").first().text();

        assertThat(entry1Text, equalTo(item1Hash));
        assertThat(entry2Text, equalTo(item2Hash));
    }

    @Test
    public void getEntriesAsJson() throws JSONException, IOException {
        Response response = getRequest("/entries.json");

        assertThat(response.getStatus(), equalTo(200));

        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);
        assertThat(res.isArray(), equalTo(true));
        assertEntryInJsonNode(res.get(0), 1, item1Hash);
        assertEntryInJsonNode(res.get(1), 2, item2Hash);
    }

    @Test
    public void getEntryByEntryNumber() throws JSONException, IOException {
        Response response = getRequest("/entry/1.json");

        assertThat(response.getStatus(), equalTo(200));
        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);
        assertEntryInJsonNode(res, 1, item1Hash);
    }

    @Test
    public void entryView_itemHashIsRenderedAsALink() {
        Response response = getRequest("/entry/1");

        Document doc = Jsoup.parse(response.readEntity(String.class));
        String text = doc.getElementsByTag("table").select("a[href=/item/" + item1Hash + "]").first().text();
        assertThat(text, equalTo(item1Hash));
    }

    @Test
    public void return404ResponseWhenEntryNotExist() {
        assertThat(getRequest("/entry/5001").getStatus(), equalTo(404));
    }

    @Test
    public void return404ResponseWhenEntryNumberIsNotAnIntegerValue() {
        assertThat(getRequest("/entry/a2").getStatus(), equalTo(404));
    }

    @Test
    public void entryResource_retrieviesTimestampsInUTC() throws IOException {
        String expectedDateTime = formatInstant(Instant.now(), Optional.of(ChronoUnit.MINUTES));

        Response response = getRequest("/entry/1.json");

        Map<String, String> responseData = response.readEntity(Map.class);
        Instant entryTimestamp = Instant.parse(responseData.get("entry-timestamp"));
        String actualDateTime = formatInstant(entryTimestamp, Optional.of(ChronoUnit.MINUTES));

        assertThat(actualDateTime, equalTo(expectedDateTime));
    }

    private String formatInstant(Instant instant, Optional<TemporalUnit> temporalUnit){
        return ISO_INSTANT.format(instant.truncatedTo(temporalUnit.orElseGet(() -> ChronoUnit.SECONDS)));
    }

    private void assertEntryInJsonNode(JsonNode actualJsonNode, int expectedEntryNumber, String expectedHash){
        assertThat(actualJsonNode.get("entry-number").asInt(), equalTo(expectedEntryNumber));
        assertThat(actualJsonNode.get("item-hash").textValue(), equalTo(expectedHash));
        assertTrue(actualJsonNode.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));

    }
}
