package uk.gov.register.functional;

import io.dropwizard.testing.ConfigOverride;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
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
}
