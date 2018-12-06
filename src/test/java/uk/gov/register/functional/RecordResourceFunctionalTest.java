package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static uk.gov.register.functional.app.TestRegister.address;

public class RecordResourceFunctionalTest {
    @ClassRule
    public static RegisterRule register = new RegisterRule();

    @Before
    public void publishTestMessages() throws Throwable {
        register.wipe();
        register.loadRsfV1(address, RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER + addressRsf());
    }

    @Test
    public void getRecordByKey() throws JSONException, IOException {
        Response response = register.getRequest(address, "/record/6789.json");

        assertThat(response.getStatus(), equalTo(200));

        assertThat(response.getHeaderString("Link"), equalTo("</records/6789/entries>; rel=\"version-history\""));

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
    public void getRecords() throws IOException {
        Response response = register.getRequest(address, "/records.json");
        
        assertThat(response.getStatus(), equalTo(200));

        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);

        JsonNode firstRecord = res.get("145678");
        assertThat(firstRecord.get("entry-number").textValue(), equalTo("3"));
        assertThat(firstRecord.get("index-entry-number").textValue(), equalTo("3"));
        assertTrue(firstRecord.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));
        assertThat(firstRecord.get("item").size(), equalTo(1));

        JsonNode secondRecord = res.get("6789");
        assertThat(secondRecord.get("entry-number").textValue(), equalTo("2"));
        assertThat(secondRecord.get("index-entry-number").textValue(), equalTo("2"));
        assertTrue(secondRecord.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));
        assertThat(secondRecord.get("item").size(), equalTo(1));
    }

    @Test
    public void getFacetedRecords() throws IOException {
        Response response = register.getRequest(address, "/records/street/presley.json");

        assertThat(response.getStatus(), equalTo(200));

        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);

        assertThat(res.size(), equalTo(1));
        JsonNode facetedRecord = res.get("6789");
        assertThat(facetedRecord.get("entry-number").textValue(), equalTo("2"));
        assertThat(facetedRecord.get("index-entry-number").textValue(), equalTo("2"));
        assertTrue(facetedRecord.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));
        assertThat(facetedRecord.get("item").size(), equalTo(1));
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
    public void historyResource_returnsHistoryOfARecord() throws IOException {
        Response response = register.getRequest(address, "/record/6789/entries.json");

        assertThat(response.getStatus(), equalTo(200));

        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);

        assertThat(res.isArray(), equalTo(true));

        JsonNode firstEntry = res.get(0);
        assertThat(firstEntry.get("index-entry-number").textValue(), equalTo("1"));
        assertThat(firstEntry.get("entry-number").textValue(), equalTo("1"));
        assertThat(firstEntry.get("item-hash").get(0).textValue(), equalTo("sha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786" ));
        assertTrue(firstEntry.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));

        JsonNode secondEntry = res.get(1);
        assertThat(secondEntry.get("index-entry-number").textValue(), equalTo("2"));
        assertThat(secondEntry.get("entry-number").textValue(), equalTo("2"));
        assertThat(secondEntry.get("item-hash").get(0).textValue(), equalTo("sha-256:bd239db51960376826b937a615f0f3397485f00611d35bb7e951e357bf73b934" ));
        assertTrue(secondEntry.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));
    }

    @Test
    public void historyResource_return404ResponseWhenRecordNotExist() {
        assertThat(register.getRequest(address, "/record/5001/entries.json").getStatus(), equalTo(404));
    }

    @Test
    public void facetedRecordResource_return404ResponseWhenFieldNotExist() {
        assertThat(register.getRequest(address, "/records/invalidfield/presley").getStatus(), equalTo(404));
    }

    @Test
    public void facetedRecordResource_return200EmptyResponseWhenFieldValueNotExist() throws IOException {
        Response response = register.getRequest(address, "/records/street/invalid.json");

        assertThat(response.getStatus(), equalTo(200));

        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);

        assertThat(res.size(), equalTo(0));
    }

    private String addressRsf(){
        return "add-item\t{\"address\":\"6789\",\"street\":\"elvis\"}\n" +
                "append-entry\tuser\t6789\t2017-06-09T12:09:02Z\tsha-256:9432331d3343a7ceaaee46308069d01836460294c672223b236727a790acf786\n" +
                "add-item\t{\"address\":\"6789\",\"street\":\"presley\"}\n" +
                "append-entry\tuser\t6789\t2017-06-09T12:09:02Z\tsha-256:bd239db51960376826b937a615f0f3397485f00611d35bb7e951e357bf73b934\n" +
                "add-item\t{\"address\":\"145678\",\"street\":\"ellis\"}\n" +
                "append-entry\tuser\t145678\t2017-06-09T12:09:02Z\tsha-256:8ac926428ee49fb83c02bdd2556e62e84cfd9e636cd35eb1306ac8cb661e4983";
    }
}
