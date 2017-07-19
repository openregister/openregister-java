package uk.gov.register.functional;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;
import uk.gov.register.functional.app.TestRegister;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.functional.app.TestRegister.postcode;

@RunWith(Parameterized.class)
public class DeleteRegisterDataAvailabilityFunctionalTest {

    private static final TestRegister REGISTER_WHICH_ALLOWS_DELETE = postcode;
    private static final TestRegister REGISTER_WHICH_DENIES_DELETE = address;
    private static final String DELETE_ENDPOINT = "/delete-register-data";
    private final Boolean enableRegisterDataDelete;

    @ClassRule
    public static final RegisterRule register = new RegisterRule();
    
    @Before
    public void setup() {
        register.loadRsf(address, RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER);
    }

    private final Boolean isAuthenticated;
    private final int expectedStatusCode;

    public DeleteRegisterDataAvailabilityFunctionalTest(Boolean enableRegisterDataDelete, Boolean isAuthenticated, int expectedStatusCode) {
        this.enableRegisterDataDelete = enableRegisterDataDelete;
        this.isAuthenticated = isAuthenticated;
        this.expectedStatusCode = expectedStatusCode;
    }

    @Parameterized.Parameters(name = "{index}: with registerDataDelete enabled:{0} and authenticated:{1} returns {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {true, true, 200},
                {true, false, 401},
                {false, true, 404},
                {false, false, 404}
        });
    }

    @Test
    public void checkDeleteRegisterDataStatusCode() throws Exception {
        TestRegister testRegister = enableRegisterDataDelete ? REGISTER_WHICH_ALLOWS_DELETE : REGISTER_WHICH_DENIES_DELETE;
        Response response = isAuthenticated ? register.deleteRegisterData(testRegister) : makeUnauthenticatedDeleteCallTo(testRegister, DELETE_ENDPOINT);

        assertThat(response.getStatus(), equalTo(expectedStatusCode));
    }

    private Response makeUnauthenticatedDeleteCallTo(TestRegister testRegister, String endpoint) {
        // register.target() is unauthenticated
        return register.target(testRegister).path(endpoint).request().delete();
    }
}
