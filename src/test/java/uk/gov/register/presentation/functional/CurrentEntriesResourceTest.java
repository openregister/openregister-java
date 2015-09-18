package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CurrentEntriesResourceTest extends FunctionalTestBase {

    //TODO: resolve data loading duplication in all tests
    @BeforeClass
    public static void publishTestMessages() {
        publishMessagesToDB(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}"
        ));
    }

    @Test
    public void feed_returnsNextAndPreviousPageLinkHeadersWhenAvailable() {
        Map<String, String> relToLinkMap = createRelToLinkMap(getRequest("/feed.json?pageIndex=1&pageSize=3"));
        assertThat(relToLinkMap.size(), equalTo(0));

        String requestUrl = "/feed.json?pageIndex=1&pageSize=1";
        relToLinkMap = createRelToLinkMap(getRequest(requestUrl));
        assertThat(relToLinkMap.size(), equalTo(1));
        assertThat(relToLinkMap.get("next"), equalTo("/feed?pageIndex=2&pageSize=1"));

        requestUrl = "/feed.json?pageIndex=2&pageSize=1";
        relToLinkMap = createRelToLinkMap(getRequest(requestUrl));
        assertThat(relToLinkMap.size(), equalTo(2));
        assertThat(relToLinkMap.get("next"), equalTo("/feed?pageIndex=3&pageSize=1"));
        assertThat(relToLinkMap.get("previous"), equalTo("/feed?pageIndex=1&pageSize=1"));


        requestUrl = "/feed.json?pageIndex=3&pageSize=1";
        relToLinkMap = createRelToLinkMap(getRequest(requestUrl));
        assertThat(relToLinkMap.size(), equalTo(1));
        assertThat(relToLinkMap.get("previous"), equalTo("/feed?pageIndex=2&pageSize=1"));

    }

    @Test
    public void current_returnsNextAndPreviousPageLinkHeadersWhenAvailable() {
        Map<String, String> relToLinkMap = createRelToLinkMap(getRequest("/current.json?pageIndex=1&pageSize=3"));
        assertThat(relToLinkMap.size(), equalTo(0));

        String requestUrl = "/current.json?pageIndex=1&pageSize=1";
        relToLinkMap = createRelToLinkMap(getRequest(requestUrl));
        assertThat(relToLinkMap.size(), equalTo(1));
        assertThat(relToLinkMap.get("next"), equalTo("/current?pageIndex=2&pageSize=1"));

        requestUrl = "/current.json?pageIndex=2&pageSize=1";
        relToLinkMap = createRelToLinkMap(getRequest(requestUrl));
        assertThat(relToLinkMap.size(), equalTo(2));
        assertThat(relToLinkMap.get("next"), equalTo("/current?pageIndex=3&pageSize=1"));
        assertThat(relToLinkMap.get("previous"), equalTo("/current?pageIndex=1&pageSize=1"));


        requestUrl = "/current.json?pageIndex=3&pageSize=1";
        relToLinkMap = createRelToLinkMap(getRequest(requestUrl));
        assertThat(relToLinkMap.size(), equalTo(1));
        assertThat(relToLinkMap.get("previous"), equalTo("/current?pageIndex=2&pageSize=1"));

    }

    @Test
    public void current_shouldReturnAllCurrentVersionsOnly() throws Exception {
        Response response = getRequest("/current.json");

        String jsonResponse = response.readEntity(String.class);
        JSONAssert.assertEquals(jsonResponse,
                "[" +
                        "{\"serial-number\":2,\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}," +
                        "{\"serial-number\":3,\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}," +
                        "{\"serial-number\":1,\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}" +
                        "]"
                , false);
    }

    @Test
    public void all_movedPermanentlyToCurrentSoReturns301() throws InterruptedException, IOException {
        Response response = getRequest("/all.json");

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://address.beta.openregister.org/current.json"));

    }

    private Map<String, String> createRelToLinkMap(Response response) {
        String link = response.getHeaderString("Link");
        return StringUtils.isNotEmpty(link) ? Stream.of(link.split(","))
                .map(h -> h.split(";"))
                .collect(Collectors.toMap(h -> h[1].trim().replaceAll("rel=\"([a-z]+)\"", "$1"), h -> h[0].trim().replaceAll("<([^>]+)>", "$1")))
                : Collections.emptyMap();
    }
}

