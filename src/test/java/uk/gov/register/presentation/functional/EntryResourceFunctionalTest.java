package uk.gov.register.presentation.functional;

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
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class EntryResourceFunctionalTest extends FunctionalTestBase {
    private static final String item1 = "{\"address\":\"6789\",\"street\":\"presley\"}";
    private static final String item2 = "{\"address\":\"6789\",\"street\":\"presley\"}";

    @Before
    public void publishTestMessages() throws Throwable {
        mintItems(item1, item2);
    }

    @Test
    public void getEntryByEntryNumber() throws JSONException, IOException {
        String sha256Hex = DigestUtils.sha256Hex(item1);

        Response response = getRequest("/entry/1.json");

        assertThat(response.getStatus(), equalTo(200));

        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);

        assertThat(res.get("entry-number").textValue(), equalTo("1"));
        assertThat(res.get("item-hash").textValue(), equalTo("sha-256:" + sha256Hex));
        assertTrue(res.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));
    }

    @Test
    public void entryView_itemHashIsRenderedAsALink() {
        String sha256Hex = DigestUtils.sha256Hex(item1);

        Response response = getRequest("/entry/1");
        Document doc = Jsoup.parse(response.readEntity(String.class));
        String text = doc.getElementsByTag("table").select("a[href=/item/sha-256:" + sha256Hex + "]").first().text();
        assertThat(text, equalTo("sha-256:" + sha256Hex));
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
        String dateTimeWithNoSeconds = "yyyyMMddHHmm";
        String expectedDateTime = formatInstant(Instant.now(), dateTimeWithNoSeconds);

        Response response = getRequest("/entry/1.json");

        Map<String, String> responseData = response.readEntity(Map.class);
        Instant entryTimestamp = Instant.parse(responseData.get("entry-timestamp"));
        String actualDateTime = formatInstant(entryTimestamp, dateTimeWithNoSeconds);

        assertThat(actualDateTime, equalTo(expectedDateTime));
    }

    private String formatInstant(Instant instant, String format){
        return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern(format));
    }
}
