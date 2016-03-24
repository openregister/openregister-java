package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ItemResourceFunctionalTest extends FunctionalTestBase {
    static String item1 = "{\"address\":\"6789\",\"name\":\"presley\"}";
    static String item2 = "{\"address\":\"145678\",\"name\":\"ellis\"}";

    @Before
    public void publishTestMessages() throws Throwable {
        cleanDatabaseRule.before();
        dbSupport.publishMessages(ImmutableList.of(
                String.format("{\"hash\":\"hash1\",\"entry\":%s}", item1),
                String.format("{\"hash\":\"hash2\",\"entry\":%s}", item2)
        ));
    }

    @Test
    public void jsonRepresentationOfAnItem() throws JSONException {
        String sha256Hex = DigestUtils.sha256Hex(item1);

        Response response = getRequest(String.format("/item/sha-256:%s.json", sha256Hex));

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaders().get("cache-control").toString(), equalTo("[no-transform, max-age=31536000]"));

        JSONAssert.assertEquals(item1, response.readEntity(String.class), false);
    }

    @Test
    public void return404ResponseWhenItemNotExist() throws JSONException {
        Response response = getRequest("/item/sha-256:notExistHexValue");

        assertThat(response.getStatus(), equalTo(404));
    }

    //Note: tests below are valid till migration is in progress, delete after migration and respective code in ItemResource
    @Test
    public void return404ResponseWhenItemTableNotExist() {
        testDAO.testItemDAO.dropTable();
        String sha256Hex = DigestUtils.sha256Hex(item1);

        Response response = getRequest("/item/sha-256:" + sha256Hex);

        assertThat(response.getStatus(), equalTo(404));
    }
}
