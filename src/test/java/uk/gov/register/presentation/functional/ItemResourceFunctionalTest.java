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
import static uk.gov.register.presentation.functional.TestEntry.anEntry;

public class ItemResourceFunctionalTest extends FunctionalTestBase {
    private static String item1 = "{\"address\":\"6789\",\"name\":\"presley\"}";
    private static String item2 = "{\"address\":\"145678\",\"name\":\"ellis\"}";

    @Before
    public void publishTestMessages() throws Throwable {
        dbSupport.publishEntries(ImmutableList.of(
                anEntry(1, item1),
                anEntry(2, item2)
        ));
    }

    @Test
    public void jsonRepresentationOfAnItem() throws JSONException {
        String sha256Hex = DigestUtils.sha256Hex(item1);

        Response response = getRequest(String.format("/item/sha-256:%s.json", sha256Hex));

        assertThat(response.getStatus(), equalTo(200));

        JSONAssert.assertEquals(item1, response.readEntity(String.class), false);
    }

    @Test
    public void return404ResponseWhenItemNotExist() throws JSONException {
        Response response = getRequest("/item/sha-256:notExistHexValue");

        assertThat(response.getStatus(), equalTo(404));
    }

    @Test
    public void return404ResponseWhenItemHexIsNotInProperFormat() throws JSONException {
        Response response = getRequest("/item/notExistHexValue");

        assertThat(response.getStatus(), equalTo(404));
    }

}
