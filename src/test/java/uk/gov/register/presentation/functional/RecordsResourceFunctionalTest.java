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

public class RecordsResourceFunctionalTest extends FunctionalTestBase {
    @BeforeClass
    public static void publishTestMessages() {
        DBSupport.publishMessages(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}"
        ));
    }

    @Test
    public void records_shouldReturnAllCurrentVersionsOnly() throws Exception {
        Response response = getRequest("/records.json");

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
    public void current_movedPermanentlyToRecordsSoReturns301() throws InterruptedException, IOException {
        Response response = getRequest("/current.json");

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://address.beta.openregister.org/records.json"));

    }

    @Test
    public void records_hasLinkHeaderForNextAndPreviousPage() {
        Response response = getRequest("/records.json?page-index=1&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("</records?page-index=2&page-size=1>; rel=\"next\""));

        response = getRequest("/records.json?page-index=2&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("</records?page-index=3&page-size=1>; rel=\"next\",</records?page-index=1&page-size=1>; rel=\"previous\""));

        response = getRequest("/records.json?page-index=3&page-size=1");
        assertThat(response.getHeaderString("Link"), equalTo("</records?page-index=2&page-size=1>; rel=\"previous\""));
    }
}

