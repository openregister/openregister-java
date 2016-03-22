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

public class ItemResourceTest extends FunctionalTestBase {
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
    public void getItemBySha256Hash() throws JSONException {
        String sha256Hex = DigestUtils.sha256Hex(item1);

        Response response = getRequest("/item/sha-256:" + sha256Hex);

        assertThat(response.getStatus(), equalTo(200));

        JSONAssert.assertEquals(item1, response.readEntity(String.class), false);
    }

    @Test
    public void return404ResponseWhenItemNotExist() throws JSONException {
        String sha256Hex = DigestUtils.sha256Hex(item1);

        Response response = getRequest("/item/sha256:" + sha256Hex);

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
