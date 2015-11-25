package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.register.presentation.representations.DBSupport;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FindEntityTest extends FunctionalTestBase {

    @BeforeClass
    public static void publishTestMessages() {
        DBSupport.publishMessages(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}"
        ));
    }

    @Test
    public void find_shouldReturnEntryWithThPrimaryKey_whenSearchForPrimaryKey() throws JSONException {
        Response response = getRequest("/address/12345.json");

        assertThat(response.getHeaderString("Link"), equalTo("</address/12345/history>;rel=\"version-history\""));
        JSONAssert.assertEquals("{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}", response.readEntity(String.class), false);
    }

    @Test
    public void findByHash_shouldReturnEntryForTheGivenHash() throws Exception {
        Response response = getRequest("/entry/2.json");

        assertThat(response.getHeaderString("Link"), equalTo("</address/6789/history>;rel=\"version-history\""));
        JSONAssert.assertEquals("{" +
                "\"serial-number\":2," +
                "\"hash\":\"hash2\"," +
                "\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}"
                , response.readEntity(String.class), false);
    }
}
