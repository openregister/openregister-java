package uk.gov.register.functional;

import io.dropwizard.testing.ConfigOverride;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DeleteRegisterDataFunctionalTest {
    public static final String REGISTER_WHICH_ALLOWS_DELETING = "postcode";
    @ClassRule
    public static final RegisterRule register = new RegisterRule(
            ConfigOverride.config("enableRegisterDataDelete", "true"));

    @Test
    public void deleteRegisterData_deletesAllDataFromDb() throws Exception {
        String item1 = "{\"postcode\":\"P1\"}";
        String item2 = "{\"postcode\":\"P2\"}";

        Response mintResponse = register.mintLines(REGISTER_WHICH_ALLOWS_DELETING, item1, item2);
        assertThat(mintResponse.getStatus(), equalTo(204));

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
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        String item2 = "{\"register\":\"register2\",\"text\":\"Register2 Text\", \"phase\":\"alpha\"}";

        register.deleteRegisterData();
        register.mintLines(item1, item2);

        Response proof1Response = register.getRequest("/proof/register/merkle:sha-256");
        assertThat(proof1Response.getStatus(), equalTo(200));
        String proof1 = proof1Response.readEntity(String.class);

        register.deleteRegisterData();
        register.mintLines(item2, item1);

        Response proof2Response = register.getRequest("/proof/register/merkle:sha-256");
        assertThat(proof2Response.getStatus(), equalTo(200));
        String proof2 = proof2Response.readEntity(String.class);

        assertThat(proof2, not(equalTo(proof1)));
    }
}
