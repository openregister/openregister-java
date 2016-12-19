package uk.gov.register.functional;

import io.dropwizard.testing.ConfigOverride;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DeleteRegisterDataFunctionalTest {
    @Rule
    public final RegisterRule register = new RegisterRule(
            ConfigOverride.config("enableRegisterDataDelete", "true"));

    @Test
    public void deleteRegisterData_deletesAllDataFromDb() throws Exception {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        String item2 = "{\"register\":\"register2\",\"text\":\"Register2 Text\", \"phase\":\"alpha\"}";

        Response mintResponse = register.mintLines("register", item1, item2);
        assertThat(mintResponse.getStatus(), equalTo(204));

        Response entriesResponse1 = register.getRequest("register", "/entries.json");
        List<?> entriesList = entriesResponse1.readEntity(List.class);
        assertThat(entriesList, hasSize(2));

        Response deleteResponse = register.deleteRegisterData("register");
        assertThat(deleteResponse.getStatus(), equalTo(200));

        Response entriesResponse2 = register.getRequest("register", "/entries.json");
        String entriesRawJSON = entriesResponse2.readEntity(String.class);

        assertThat(entriesRawJSON, is("[]"));
    }
}
