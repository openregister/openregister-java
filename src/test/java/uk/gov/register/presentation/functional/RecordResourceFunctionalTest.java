package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class RecordResourceFunctionalTest extends FunctionalTestBase {
    private final static String item0 = "{\"address\":\"6789\",\"name\":\"elvis\"}";
    private final static String item1 = "{\"address\":\"6789\",\"name\":\"presley\"}";

    @Before
    public void publishTestMessages() throws Throwable {
        dbSupport.publishMessages(ImmutableList.of(
                "{\"hash\":\"hash0\",\"entry\":" + item0 + "}",
                "{\"hash\":\"hash1\",\"entry\":" + item1 + "}",
                "{\"hash\":\"hash2\",\"entry\":{\"address\":\"145678\",\"name\":\"ellis\"}}"
        ));
    }

    @Test
    public void getRecordByKey() throws JSONException, IOException {
        String sha256Hex = DigestUtils.sha256Hex(item1);

        Response response = getRequest("/record/6789.json");

        assertThat(response.getStatus(), equalTo(200));

        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);
        assertThat(res.get("entry-number").textValue(), equalTo("2"));
        assertThat(res.get("item-hash").textValue(), equalTo("sha-256:" + sha256Hex));
        assertThat(res.get("address").textValue(), equalTo("6789"));
        assertThat(res.get("name").textValue(), equalTo("presley"));
        assertTrue(res.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));
    }

    @Test
    public void recordResource_return404ResponseWhenRecordNotExist() {
        assertThat(getRequest("/record/5001.json").getStatus(), equalTo(404));
    }

    @Test
    public void historyResource_returnsHistoryOfARecord() throws IOException {
        Response response = getRequest("/record/6789/entries.json");

        assertThat(response.getStatus(), equalTo(200));

        JsonNode res = Jackson.newObjectMapper().readValue(response.readEntity(String.class), JsonNode.class);

        assertThat(res.isArray(), equalTo(true));

        JsonNode firstEntry = res.get(0);
        assertThat(firstEntry.get("entry-number").textValue(), equalTo("1"));
        assertThat(firstEntry.get("item-hash").textValue(), equalTo("sha-256:" + DigestUtils.sha256Hex(item0)));
        assertTrue(firstEntry.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));

        JsonNode secondEntry = res.get(1);
        assertThat(secondEntry.get("entry-number").textValue(), equalTo("2"));
        assertThat(secondEntry.get("item-hash").textValue(), equalTo("sha-256:" + DigestUtils.sha256Hex(item1)));
        assertTrue(secondEntry.get("entry-timestamp").textValue().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"));

    }

    @Test
    public void historyResource_return404ResponseWhenRecordNotExist() {
        assertThat(getRequest("/record/5001/entries.json").getStatus(), equalTo(404));
    }

}
