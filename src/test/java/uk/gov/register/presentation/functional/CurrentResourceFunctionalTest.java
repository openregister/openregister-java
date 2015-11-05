package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.register.presentation.representations.DBSupport;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CurrentResourceFunctionalTest extends FunctionalTestBase {
    @BeforeClass
    public static void publishTestMessages() {
        DBSupport.publishMessages(ImmutableList.of(
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
    public void current_hasLinkHeaderForNextAndPreviousPage() {
        Response response = getRequest("/current.json?pageIndex=1&pageSize=1");
        assertThat(response.getHeaderString("Link"), equalTo("</current?pageIndex=2&pageSize=1>; rel=\"next\""));

        response = getRequest("/current.json?pageIndex=2&pageSize=1");
        assertThat(response.getHeaderString("Link"), equalTo("</current?pageIndex=3&pageSize=1>; rel=\"next\",</current?pageIndex=1&pageSize=1>; rel=\"previous\""));

        response = getRequest("/current.json?pageIndex=3&pageSize=1");
        assertThat(response.getHeaderString("Link"), equalTo("</current?pageIndex=2&pageSize=1>; rel=\"previous\""));
    }
}

