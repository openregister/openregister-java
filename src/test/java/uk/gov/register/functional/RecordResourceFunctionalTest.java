package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.dropwizard.jackson.Jackson;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static uk.gov.register.functional.app.TestRegister.address;

public class RecordResourceFunctionalTest {
    private final static String item0 = "{\"address\":\"6789\",\"street\":\"elvis\"}";
    private final static String item1 = "{\"address\":\"6789\",\"street\":\"presley\"}";
    @ClassRule
    public static RegisterRule register = new RegisterRule();

    @Before
    public void publishTestMessages() throws Throwable {
        register.wipe();
        register.mintLines(address, item0, item1, "{\"address\":\"145678\",\"street\":\"ellis\"}");
    }

    @Test
    public void getRecordByKey() throws JSONException, IOException {
        String sha256Hex = DigestUtils.sha256Hex(item1);

        Response response = register.getRequest(address, "/record/6789.json");

        assertThat(response.getStatus(), equalTo(200));

        assertThat(response.getHeaderString("Link"), equalTo("</record/6789/entries>; rel=\"version-history\""));

        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class).get("6789");
        assertThat(res.get("entry-number").textValue(), equalTo("2"));
        assertThat(res.get("key").textValue(), equalTo("6789"));
        assertTrue(res.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));

        ArrayNode items = (ArrayNode)res.get("item");
        assertThat(items.size(), is(1));
        JsonNode itemMap = items.get(0);
        assertThat(itemMap.get("street").asText(), is("presley"));
        assertThat(itemMap.get("address").asText(), is("6789"));
    }

    @Test
    public void recordResource_return404ResponseWhenRecordNotExist() {
        assertThat(register.getRequest(address, "/record/5001.json").getStatus(), equalTo(404));
    }

    @Test
    public void recordResource_return400ResponseWhenPageSizeIsNotANumber() {
        Response response = register.target(address).path("/records").queryParam("page-size", "not-a-number").request().get();

        assertThat(response.getMediaType().getType(), equalTo("text"));
        assertThat(response.getMediaType().getSubtype(), equalTo("html"));
        assertThat(response.getStatus(), equalTo(400));
    }

    @Test
    public void recordResource_return400ResponseWhenPageIndexIsNotANumber() {
        Response response = register.target(address).path("/records").queryParam("page-index", "not-a-number").request().get();

        assertThat(response.getMediaType().getType(), equalTo("text"));
        assertThat(response.getMediaType().getSubtype(), equalTo("html"));
        assertThat(response.getStatus(), equalTo(400));
    }

    @Test
    public void recordResource_return200ResponseForTextHtmlMediaTypeWhenRecordExists() {
        Response response = register.getRequest(address, "/record/6789", MediaType.TEXT_HTML);

        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void recordResource_returnsMostRecentRecord_whenMultipleEntriesExist() throws IOException {
        Response response = register.getRequest(address, "/record/6789.json");
        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);

        assertThat(res.get("6789").get("item").get(0).get("street").textValue(), equalTo("presley"));
    }

    @Test
    public void recordResource_returnsIdenticalResponseAsIndexEndpoint() throws IOException {
        String indexResponse = register.getRequest(address, "/index/records/record/6789.json").readEntity(String.class);
        String recordsResponse = register.getRequest(address, "/record/6789.json").readEntity(String.class);

        assertThat(indexResponse, equalTo(recordsResponse));
    }

    @Test
    public void historyResource_returnsHistoryOfARecord() throws IOException {
        Response response = register.getRequest(address, "/record/6789/entries.json");

        assertThat(response.getStatus(), equalTo(200));

        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);

        assertThat(res.isArray(), equalTo(true));

        JsonNode firstEntry = res.get(0);
        assertThat(firstEntry.get("index-entry-number").textValue(), equalTo("1"));
        assertThat(firstEntry.get("entry-number").textValue(), equalTo("1"));
        assertThat(firstEntry.get("item-hash").get(0).textValue(), equalTo("sha-256:" + DigestUtils.sha256Hex(item0)));
        assertTrue(firstEntry.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));

        JsonNode secondEntry = res.get(1);
        assertThat(secondEntry.get("index-entry-number").textValue(), equalTo("2"));
        assertThat(secondEntry.get("entry-number").textValue(), equalTo("2"));
        assertThat(secondEntry.get("item-hash").get(0).textValue(), equalTo("sha-256:" + DigestUtils.sha256Hex(item1)));
        assertTrue(secondEntry.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));
    }

    @Test
    public void historyResource_return404ResponseWhenRecordNotExist() {
        assertThat(register.getRequest(address, "/record/5001/entries.json").getStatus(), equalTo(404));
    }
}
