package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FindEntityTest extends FunctionalTestBase {

    public static ObjectMapper OBJECT_MAPPER;

    @BeforeClass
    public static void publishTestMessages() {
        publishMessagesToDB(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}"
        ));
        OBJECT_MAPPER = Jackson.newObjectMapper();
    }

    @Test
    public void findByPrimaryKey_shouldReturnEntryWithThPrimaryKey() {
        Response response = getRequest("/address/12345.json");

        assertThat(response.readEntity(String.class), equalTo("{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}"));
    }

    @Test
    public void findByPrimaryKey_returns400ForNonPrimaryKeySearch() {
        Response response = getRequest("/key1/key1Value_1.json");

        assertThat(response.getStatus(), equalTo(404));

    }

    @Test
    public void findByHash_shouldReturnEntryForTheGivenHash() throws IOException {
        Response response = getRequest("/hash/hash2.json");

        assertThat(OBJECT_MAPPER.readValue(response.readEntity(String.class), JsonNode.class),
                equalTo(OBJECT_MAPPER.readValue("{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}", JsonNode.class)));
    }

    @Test
    public void all_movedPermanentlyToCurrentSoReturns301() throws InterruptedException, IOException {
        Response response = getRequest("/all.json");

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://address.beta.openregister.org/current.json"));

    }

    @Test
    public void latest_movedPermanentlyToCurrentSoReturns301() throws InterruptedException, IOException {
        Response response = getRequest("/latest.json");

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://address.beta.openregister.org/feed.json"));

    }

    @Test
    public void current_shouldReturnAllCurrentVersionsOnly() throws InterruptedException, IOException {
        Response response = getRequest("/current.json");

        String jsonResponse = response.readEntity(String.class);
        assertThat(OBJECT_MAPPER.readValue(jsonResponse, JsonNode.class),
                equalTo(OBJECT_MAPPER.readValue("[{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}},{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}},{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}]", JsonNode.class)));
    }

    @Test
    public void search_returnsAllMatchingEntries() {
        Response response = getRequest("/search.json?name=ellis");

        assertThat(response.readEntity(String.class), equalTo(
                "[{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}},{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}]"
        ));
    }

    @Test
    public void search_returnsAllEntriesWhenNoSearchQueryIsGiven() {
        Response response = getRequest("/search.json");

        assertThat(response.readEntity(String.class), equalTo(
                "[{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}},{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}},{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}]"
        ));
    }

}
