package uk.gov.register.functional;

import io.dropwizard.testing.ConfigOverride;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.register.functional.db.TestDBSupport.testEntryDAO;
import static uk.gov.register.functional.db.TestDBSupport.testItemDAO;

public class DeleteRegisterDataFunctionalTest {
    @Rule
    public final RegisterRule register = new RegisterRule("register",
            ConfigOverride.config("enableRegisterDataDelete", "true"));

    @Test
    public void deleteRegisterData_deletesAllDataFromDb() throws Exception {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        String item2 = "{\"register\":\"register2\",\"text\":\"Register2 Text\", \"phase\":\"alpha\"}";

        Response mintResponse = register.mintLines(item1 + "\n" + item2);
        assertThat(mintResponse.getStatus(), equalTo(204));
        assertThat(testItemDAO.getItems(), hasSize(2));
        assertThat(testEntryDAO.getAllEntries(), hasSize(2));

        Response deleteResponse = register.deleteRequest("/delete-register-data");

        assertThat(deleteResponse.getStatus(), equalTo(200));
        assertThat(testItemDAO.getItems(), is(empty()));
        assertThat(testEntryDAO.getAllEntries(), is(empty()));
    }
}
