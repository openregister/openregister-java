package uk.gov.register.functional;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;
import uk.gov.register.functional.app.TestRegister;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.register.functional.app.TestRegister.postcode;

@Ignore("Relies on schemas being migrated when the app starts")
public class DeleteRegisterDataFunctionalTest {
    public static final TestRegister REGISTER_WHICH_ALLOWS_DELETING = postcode;
    @ClassRule
    public static final RegisterRule register = new RegisterRule();

    @Test
    public void deleteRegisterData_deletesAllDataFromDb() throws Exception {
        register.loadRsf(postcode, RsfRegisterDefinition.POSTCODE_REGISTER);

        String rsf = "add-item\t{\"postcode\":\"P1\"}\n" +
                "add-item\t{\"postcode\":\"P2\"}\n" +
                "append-entry\tuser\tP1\t2018-07-26T15:55:27Z\tsha-256:50a5de96d4cb6341a2f18c0b34bc401b2c92e3ac46641c0d1014dc82ed498326\n" +
                "append-entry\tuser\tP2\t2018-07-26T15:55:27Z\tsha-256:af0cf1489bfb0f3da8130a50e6b4fa49de7c480a416dfa710418c0e42dc72949\n";

        Response mintResponse = register.loadRsf(REGISTER_WHICH_ALLOWS_DELETING, rsf);
        assertThat(mintResponse.getStatus(), equalTo(200));

        Response entriesResponse1 = register.getRequest(REGISTER_WHICH_ALLOWS_DELETING, "/entries.json");
        List<?> entriesList = entriesResponse1.readEntity(List.class);
        assertThat(entriesList, hasSize(2));

        Response deleteResponse = register.deleteRegisterData(REGISTER_WHICH_ALLOWS_DELETING);
        assertThat(deleteResponse.getStatus(), equalTo(200));

        Response entriesResponse2 = register.getRequest(REGISTER_WHICH_ALLOWS_DELETING, "/entries.json");
        String entriesRawJSON = entriesResponse2.readEntity(String.class);

        assertThat(entriesRawJSON, is("[]"));
    }

    @Test
    public void deleteRegisterData_deletesProofCache() throws Exception {
        register.loadRsf(postcode, RsfRegisterDefinition.POSTCODE_REGISTER);

        String rsf = "add-item\t{\"postcode\":\"P1\"}\n" +
                "add-item\t{\"postcode\":\"P2\"}\n" +
                "append-entry\tuser\tP1\t2018-07-26T15:55:27Z\tsha-256:50a5de96d4cb6341a2f18c0b34bc401b2c92e3ac46641c0d1014dc82ed498326\n" +
                "append-entry\tuser\tP2\t2018-07-26T15:55:27Z\tsha-256:af0cf1489bfb0f3da8130a50e6b4fa49de7c480a416dfa710418c0e42dc72949\n";

        register.deleteRegisterData(REGISTER_WHICH_ALLOWS_DELETING);
        register.loadRsf(REGISTER_WHICH_ALLOWS_DELETING, rsf);

        Response proof1Response = register.getRequest(REGISTER_WHICH_ALLOWS_DELETING, "/proof/register/merkle:sha-256");
        assertThat(proof1Response.getStatus(), equalTo(200));
        String proof1 = proof1Response.readEntity(String.class);

        register.deleteRegisterData(REGISTER_WHICH_ALLOWS_DELETING);
        register.loadRsf(postcode, RsfRegisterDefinition.POSTCODE_REGISTER);
        register.loadRsf(REGISTER_WHICH_ALLOWS_DELETING, rsf);

        Response proof2Response = register.getRequest(REGISTER_WHICH_ALLOWS_DELETING, "/proof/register/merkle:sha-256");
        assertThat(proof2Response.getStatus(), equalTo(200));
        String proof2 = proof2Response.readEntity(String.class);

        assertThat(proof2, not(equalTo(proof1)));
    }
}
