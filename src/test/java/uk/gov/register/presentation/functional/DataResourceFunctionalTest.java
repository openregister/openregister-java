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

public class DataResourceFunctionalTest extends FunctionalTestBase {
    @BeforeClass
    public static void publishTestMessages() {
        publishMessagesToDB(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}"
        ));
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

    @Test
    public void latest_movedPermanentlyToFeedSoReturns301() throws InterruptedException, IOException {
        Response response = getRequest("/latest.json");

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://address.beta.openregister.org/feed.json"));

    }

    private Map<String, String> createRelToLinkMap(Response response) {
        String link = response.getHeaderString("Link");
        return StringUtils.isNotEmpty(link) ? Stream.of(link.split(","))
                .map(h -> h.split(";"))
                .collect(Collectors.toMap(h -> h[1].trim().replaceAll("rel=\"([a-z]+)\"", "$1"), h -> h[0].trim().replaceAll("<([^>]+)>", "$1")))
                : Collections.emptyMap();
    }
}

