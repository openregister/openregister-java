package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.collect.Iterators;
import org.apache.commons.codec.digest.DigestUtils;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.*;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.views.representations.ExtraMediaType.TEXT_HTML;

public class EntriesResourceFunctionalTest {
    @ClassRule
    public static RegisterRule register = new RegisterRule();
    private final WebTarget addressTarget = register.target(address);

    @Before
    public void setup() {
        register.wipe();
        register.loadRsf(address, RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER);
    }

    @Test
    public void entries_returnsSingleEntryWhenRegisterDefined() {
        Response response = register.getRequest(address, "/entries.json");
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(ArrayNode.class).size(), equalTo(11));
    }

    @Test
    public void entries_setsAppropriateFilenameForDownload() {
        Response response = register.getRequest(address, "/entries.json");
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION), containsString("filename=\"address-entries.json\""));
    }

    @Test
    public void entriesPageHasXhtmlLangAttributes() throws Throwable {
        Response response = register.getRequest(address, "/entries", TEXT_HTML);

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

        register.mintLines(address, item1, item2);

        Response response = register.getRequest(address, "/entries.json");
        assertThat(response.getStatus(), equalTo(200));
        ArrayNode jsonNodes = response.readEntity(ArrayNode.class);
        assertThat(jsonNodes.size(), equalTo(13));

        JsonNode entry1 = jsonNodes.get(11);
        assertThat(Iterators.size(entry1.fields()), equalTo(5));
        assertThat(entry1.get("entry-number").textValue(), equalTo("12"));
        assertThat(entry1.get("item-hash").getNodeType(), is(JsonNodeType.ARRAY));
        String hash1 = entry1.get("item-hash").get(0).asText();
        assertThat( hash1, is("sha-256:" + DigestUtils.sha256Hex(item1)));
        verifyStringIsADateSpecifiedInSpecification(entry1.get("entry-timestamp").textValue());
        assertThat(entry1.get("key").textValue(), equalTo("1234"));

        JsonNode entry2 = jsonNodes.get(12);
        assertThat(Iterators.size(entry2.fields()), equalTo(5));
        assertThat(entry2.get("entry-number").textValue(), equalTo("13"));
        String hash2 = entry2.get("item-hash").get(0).asText();
        assertThat( hash2, is("sha-256:" + DigestUtils.sha256Hex(item2)));
        verifyStringIsADateSpecifiedInSpecification(entry2.get("entry-timestamp").textValue());
        assertThat(entry2.get("key").textValue(), equalTo("6789"));

    }

    // TODO what does start = -1 mean?
    @Ignore
    public void paginationSupport(){
        String item1 = "{\"address\":\"1234\",\"street\":\"elvis\"}";
        String item2 = "{\"address\":\"6789\",\"street\":\"presley\"}";
        String item3 = "{\"address\":\"567\",\"street\":\"john\"}";

        register.mintLines(address, item1, item2, item3);

        Response response = addressTarget.path("/entries.json").queryParam("start",1).queryParam("limit",2)
                .request().get();
        ArrayNode jsonNodes = response.readEntity(ArrayNode.class);
        assertThat(jsonNodes.size(), equalTo(2));
        assertThat(jsonNodes.get(0).get("entry-number").textValue(), equalTo("1"));
        assertThat(jsonNodes.get(1).get("entry-number").textValue(), equalTo("2"));
        assertThat(response.getHeaderString("Link"), equalTo("<?start=3&limit=2>; rel=\"next\""));

        response = addressTarget.path("/entries.json").queryParam("start",2).queryParam("limit",3)
                .request().get();
        jsonNodes = response.readEntity(ArrayNode.class);
        assertThat(jsonNodes.size(), equalTo(3));
        assertThat(jsonNodes.get(0).get("entry-number").textValue(), equalTo("2"));
        assertThat(jsonNodes.get(1).get("entry-number").textValue(), equalTo("3"));
        assertThat(response.getHeaderString("Link"), equalTo("<?start=0&limit=3>; rel=\"previous\""));

        response = addressTarget.path("/entries.json").queryParam("start",2).queryParam("limit",3)
                .request().get();
        jsonNodes = response.readEntity(ArrayNode.class);
        assertThat(jsonNodes.size(), equalTo(2));
        assertThat(jsonNodes.get(0).get("entry-number").textValue(), equalTo("2"));
        assertThat(jsonNodes.get(1).get("entry-number").textValue(), equalTo("3"));
        assertThat(response.getHeaderString("Link"), equalTo("<?start=-1&limit=3>; rel=\"previous\""));

        response = addressTarget.path("/entries.json").queryParam("start",2).queryParam("limit",1)
                .request().get();
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
