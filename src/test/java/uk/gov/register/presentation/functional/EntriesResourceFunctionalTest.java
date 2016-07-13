package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterators;
import org.apache.commons.codec.digest.DigestUtils;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class EntriesResourceFunctionalTest extends FunctionalTestBase {
    @Test
    public void entries_returnsEmptyResultJsonWhenNoEntryIsAvailable() {
        Response response = getRequest("/entries.json");
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(ArrayNode.class).size(), equalTo(0));
    }

    @Test
    public void entries_setsAppropriateFilenameForDownload() {
        Response response = getRequest("address", "/entries.json");
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION), containsString("filename=\"address-entries.json\""));
    }

    @Test
    public void entriesPageHasXhtmlLangAttributes() throws Throwable {
        Response response = getRequest("address", "/entries");

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Elements htmlElement = doc.select("html");
        assertThat(htmlElement.size(), equalTo(1));
        assertThat(htmlElement.first().attr("lang"), equalTo("en"));
        assertThat(htmlElement.first().attr("xml:lang"), equalTo("en"));
    }

    @Test
    public void entries_returnsListViewOfAllAvailableEntries() {
        String item1 = "{\"address\":\"1234\",\"street\":\"elvis\"}";
        String item2 = "{\"address\":\"6789\",\"street\":\"presley\"}";

        mintItems(item1, item2);

        Response response = getRequest("/entries.json");
        assertThat(response.getStatus(), equalTo(200));
        ArrayNode jsonNodes = response.readEntity(ArrayNode.class);
        assertThat(jsonNodes.size(), equalTo(2));

        JsonNode entry1 = jsonNodes.get(0);
        assertThat(Iterators.size(entry1.fields()), equalTo(3));
        assertThat(entry1.get("entry-number").textValue(), equalTo("1"));
        assertThat(entry1.get("item-hash").textValue(), equalTo("sha-256:" + DigestUtils.sha256Hex(item1)));
        verifyStringIsADateSpecifiedInSpecification(entry1.get("entry-timestamp").textValue());

        JsonNode entry2 = jsonNodes.get(1);
        assertThat(Iterators.size(entry2.fields()), equalTo(3));
        assertThat(entry2.get("entry-number").textValue(), equalTo("2"));
        assertThat(entry2.get("item-hash").textValue(), equalTo("sha-256:" + DigestUtils.sha256Hex(item2)));
        verifyStringIsADateSpecifiedInSpecification(entry2.get("entry-timestamp").textValue());
    }

    @Test
    public void paginationSupport(){
        String item1 = "{\"address\":\"1234\",\"street\":\"elvis\"}";
        String item2 = "{\"address\":\"6789\",\"street\":\"presley\"}";
        String item3 = "{\"address\":\"567\",\"street\":\"john\"}";

        mintItems(item1, item2, item3);

        Response response = getRequest("/entries.json?start=1&limit=2");
        ArrayNode jsonNodes = response.readEntity(ArrayNode.class);
        assertThat(jsonNodes.size(), equalTo(2));
        assertThat(jsonNodes.get(0).get("entry-number").textValue(), equalTo("1"));
        assertThat(jsonNodes.get(1).get("entry-number").textValue(), equalTo("2"));
        assertThat(response.getHeaderString("Link"), equalTo("<?start=3&limit=2>; rel=\"next\""));

        response = getRequest("/entries.json?start=2&limit=2");
        jsonNodes = response.readEntity(ArrayNode.class);
        assertThat(jsonNodes.size(), equalTo(2));
        assertThat(jsonNodes.get(0).get("entry-number").textValue(), equalTo("2"));
        assertThat(jsonNodes.get(1).get("entry-number").textValue(), equalTo("3"));
        assertThat(response.getHeaderString("Link"), equalTo("<?start=0&limit=2>; rel=\"previous\""));

        response = getRequest("/entries.json?start=2&limit=3");
        jsonNodes = response.readEntity(ArrayNode.class);
        assertThat(jsonNodes.size(), equalTo(2));
        assertThat(jsonNodes.get(0).get("entry-number").textValue(), equalTo("2"));
        assertThat(jsonNodes.get(1).get("entry-number").textValue(), equalTo("3"));
        assertThat(response.getHeaderString("Link"), equalTo("<?start=-1&limit=3>; rel=\"previous\""));

        response = getRequest("/entries.json?start=2&limit=1");
        jsonNodes = response.readEntity(ArrayNode.class);
        assertThat(jsonNodes.size(), equalTo(1));
        assertThat(jsonNodes.get(0).get("entry-number").textValue(), equalTo("2"));
        assertThat(response.getHeaderString("Link"), equalTo("<?start=3&limit=1>; rel=\"next\",<?start=1&limit=1>; rel=\"previous\""));
    }

    private void verifyStringIsADateSpecifiedInSpecification(String dateTimeString) {
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;
        Assert.assertThat(isoFormatter.format(isoFormatter.parse(dateTimeString)), Matchers.equalTo(dateTimeString));
    }
}
