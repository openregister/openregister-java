package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import javax.ws.rs.core.Response;

public class HistoryTest extends FunctionalTestBase {

    public static ObjectMapper OBJECT_MAPPER;

    @BeforeClass
    public static void publishTestMessages() {
        publishMessagesToDB(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"address\":\"145678\", \"name\":\"ellis\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"address\":\"12345\", \"name\":\"ellis\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"address\":\"6789\", \"name\":\"presley\"}}",
                "{\"hash\":\"hash4\",\"entry\":{\"address\":\"145678\", \"name\":\"updatedEllis\"}}"
        ));
        OBJECT_MAPPER = Jackson.newObjectMapper();
    }

    @Test
    public void history_returnAllVersionsInLatestOrder() throws JSONException {
        Response response = getRequest("/address/145678/history.json");

        JSONAssert.assertEquals(
                "[" +
                        "{\"hash\":\"hash4\"}," +
                        "{\"hash\":\"hash1\"}" +
                        "]",
                response.readEntity(String.class),
                JSONCompareMode.STRICT_ORDER
        );
    }
}
