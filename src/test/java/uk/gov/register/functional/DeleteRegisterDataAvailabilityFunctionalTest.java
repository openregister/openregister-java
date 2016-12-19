package uk.gov.register.functional;

import io.dropwizard.testing.ConfigOverride;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class DeleteRegisterDataAvailabilityFunctionalTest {

    private final String DELETE_ENDPOINT = "/delete-register-data";

    @Rule
    public RegisterRule register;

    private Boolean isAuthenticated;
    private final int expectedStatusCode;

    public DeleteRegisterDataAvailabilityFunctionalTest(Boolean enableRegisterDataDelete, Boolean isAuthenticated, int expectedStatusCode) {
        this.register = createRegister(enableRegisterDataDelete);
        this.isAuthenticated = isAuthenticated;
        this.expectedStatusCode = expectedStatusCode;
    }

    @Parameterized.Parameters(name = "{index}: with registerDataDelete enabled:{0} and authenticated:{1}  returns {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {true, true, 200},
                {true, false, 401},
                {false, true, 404},
                {false, false, 401}
        });
    }

    @Test
    public void checkDeleteRegisterDataStatusCode() throws Exception {
        Response response = isAuthenticated ? register.deleteRegisterData("register") : makeUnauthenticatedDeleteCallTo(DELETE_ENDPOINT);

        assertThat(response.getStatus(), equalTo(expectedStatusCode));
    }

    private static RegisterRule createRegister(Boolean enableRegisterDataDelete) {
        return new RegisterRule("register",
                ConfigOverride.config("enableRegisterDataDelete", enableRegisterDataDelete.toString()));
    }

    private Response makeUnauthenticatedDeleteCallTo(String endpoint) {
        // register.targetRegister() is unauthenticated
        return register.targetRegister("register").path(endpoint).request().delete();
    }
}
