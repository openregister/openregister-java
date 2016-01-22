package uk.gov.register.presentation.functional;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.register.presentation.functional.testSupport.DBSupport;

import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class RegisterProofResourceTest extends FunctionalTestBase {

    @Before
    public void setup() {
        DBSupport.writeSignedTreeHead(9876, 1453204760135L, "rootHash", "treeSignature");
    }

    @Test
    public void shouldReturnSTHJson() throws JSONException {
        Response response = getRequest("/proof/certificate-transparency.json");

        String jsonResponse = response.readEntity(String.class);

        assertThat(response.getStatus(), equalTo(200));
        JSONAssert.assertEquals("{" +
                "        \"tree_size\": 9876," +
                "        \"timestamp\": 1453204760135," +
                "        \"sha256_root_hash\": \"rootHash\"," +
                "        \"tree_head_signature\": \"treeSignature\"" +
                "    }", jsonResponse, false);
    }
}
