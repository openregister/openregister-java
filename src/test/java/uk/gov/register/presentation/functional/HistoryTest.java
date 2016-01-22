package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import uk.gov.register.presentation.functional.testSupport.DBSupport;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HistoryTest extends FunctionalTestBase {

    @BeforeClass
    public static void publishTestMessages() {
        DBSupport.publishMessages(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"address\":\"145678\", \"name\":\"ellis\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"address\":\"12345\", \"name\":\"ellis\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"address\":\"6789\", \"name\":\"presley\"}}",
                "{\"hash\":\"hash4\",\"entry\":{\"address\":\"145678\", \"name\":\"updatedEllis\"}}"
        ));
    }

    @Test
    public void history_returnAllVersionsInLatestOrder() throws JSONException {
        Response response = getRequest("/address/145678/history.json");

        JSONAssert.assertEquals(
                "[" +
                        "{\"serial-number\":4,\"hash\":\"hash4\"}," +
                        "{\"serial-number\":1,\"hash\":\"hash1\"}" +
                        "]",
                response.readEntity(String.class),
                JSONCompareMode.STRICT_ORDER
        );
    }

    @Test
    public void history_returns406ResponseWhenRequestedForYamlFormat() {
        Response response = getRequest("/address/145678/history.yaml");
        assertThat(response.getStatus(), equalTo(406));
    }

}
