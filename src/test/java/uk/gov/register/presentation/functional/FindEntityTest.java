package uk.gov.register.presentation.functional;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FindEntityTest extends FunctionalTestBase {

    @Before
    public void publishTestMessages() {
        mintItems(
                "{\"street\":\"ellis\",\"address\":\"12345\"}",
                "{\"street\":\"presley\",\"address\":\"6789\"}",
                "{\"street\":\"ellis\",\"address\":\"145678\"}"
        );
    }

    @Test
    public void find_shouldReturnEntryWithThPrimaryKey_whenSearchForPrimaryKey() throws JSONException {
        Response response = getRequest("/address/12345.json");

        assertThat(response.getStatus(), equalTo(301));
        String expectedRedirect = "http://address.beta.openregister.org:" + app.getLocalPort() + "/record/12345";
        assertThat(response.getHeaderString("Location"), equalTo(expectedRedirect));
    }

    @Test
    public void find_returnsTheCorrectTotalRecordsInPaginationHeader() {
        Response response = getRequest("/records/street/ellis");

        Document doc = Jsoup.parse(response.readEntity(String.class));

        assertThat(doc.body().getElementById("main").getElementsByAttributeValue("class", "column-two-thirds").first().text(), equalTo("2 records"));
    }

}
