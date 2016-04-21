package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
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
        dbSupport.publishMessages(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}"
        ));
    }

    @Test
    public void find_shouldReturnEntryWithThPrimaryKey_whenSearchForPrimaryKey() throws JSONException {
        Response response = getRequest("/address/12345.json");

        assertThat(response.getStatus(), equalTo(301));
        assertThat(response.getHeaderString("Location"), equalTo("http://address.beta.openregister.org/record/12345"));
    }

    @Test
    public void find_returnsTheCorrectTotalRecordsInPaginationHeader(){
        Response response = getRequest("/records/name/ellis");

        Document doc = Jsoup.parse(response.readEntity(String.class));

        assertThat(doc.body().getElementById("main").getElementsByAttributeValue("class", "column-two-thirds").first().text(), equalTo("2 records"));
    }

}
