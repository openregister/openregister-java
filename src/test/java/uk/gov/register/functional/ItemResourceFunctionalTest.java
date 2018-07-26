package uk.gov.register.functional;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.register.functional.app.TestRegister.address;

public class ItemResourceFunctionalTest {
    @ClassRule
    public static RegisterRule register = new RegisterRule();
    private static String item1 = "{\"address\":\"6789\",\"street\":\"presley\"}";
    private static String item2 = "{\"address\":\"145678\",\"street\":\"ellis\"}";

    @Before
    public void publishTestMessages() throws Throwable {
        register.wipe();
        register.loadRsf(address, RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER);

        String rsf = "add-item\t{\"address\":\"6789\",\"street\":\"presley\"}\n" +
                "add-item\t{\"address\":\"145678\",\"street\":\"ellis\"}\n" +
                "append-entry\tuser\tregister1\t2018-07-26T15:43:12Z\tsha-256:bd239db51960376826b937a615f0f3397485f00611d35bb7e951e357bf73b934\n" +
                "append-entry\tuser\tregister1\t2018-07-26T15:43:12Z\tsha-256:8ac926428ee49fb83c02bdd2556e62e84cfd9e636cd35eb1306ac8cb661e4983\n";

        register.loadRsf(address, rsf);
    }

    @Test
    public void jsonRepresentationOfAnItem() throws JSONException {
        String sha256Hex = DigestUtils.sha256Hex(item1);

        Response response = register.getRequest(address, String.format("/item/sha-256:%s.json", sha256Hex));

        assertThat(response.getStatus(), equalTo(200));

        JSONAssert.assertEquals(item1, response.readEntity(String.class), false);
    }

    @Test
    public void return200ResponseForTextHtmlMediaTypeWhenItemExists() {
        String sha256Hex = DigestUtils.sha256Hex(item1);

        Response response = register.getRequest(address, String.format("/item/sha-256:%s", sha256Hex), MediaType.TEXT_HTML);

        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void return404ResponseWhenItemNotExist() throws JSONException {
        Response response = register.getRequest(address, "/item/sha-256:notExistHexValue");

        assertThat(response.getStatus(), equalTo(404));
    }

    @Test
    public void return404ResponseWhenItemHexIsNotInProperFormat() throws JSONException {
        Response response = register.getRequest(address, "/item/notExistHexValue");

        assertThat(response.getStatus(), equalTo(404));
    }
}
