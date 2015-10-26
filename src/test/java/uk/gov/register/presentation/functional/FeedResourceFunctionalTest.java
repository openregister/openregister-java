package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FeedResourceFunctionalTest extends FunctionalTestBase {
    @Test
    public void feed_returnsEmptyResultJsonWhenNoEntryIsAvailable() {
        Response response = getRequest("/feed.json");
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(ArrayNode.class).size(), equalTo(0));
    }

    @Test
    public void latest_movedPermanentlyToFeedSoReturns301() throws InterruptedException, IOException {
        Response response = getRequest("/latest.json");

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://address.beta.openregister.org/feed.json"));

    }
}
