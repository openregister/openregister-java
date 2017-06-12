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

@RunWith(Parameterized.class)
public class DataDownloadResourceFunctionalTest {

    public static final TestRegister REGISTER_WITH_DOWNLOAD_ENABLED = TestRegister.address;
    public static final TestRegister REGISTER_WITH_DOWNLOAD_DISABLED = TestRegister.register;
    @ClassRule
    public static RegisterRule register = new RegisterRule();

    @Before
    public void setup() {
        register.loadRsf(REGISTER_WITH_DOWNLOAD_ENABLED, RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER);
        register.loadRsf(REGISTER_WITH_DOWNLOAD_DISABLED, RsfRegisterDefinition.REGISTER_REGISTER);
    }

    private final String targetUrl;
    private final int expectedStatusCode;
    private final Boolean enableDownload;

    public DataDownloadResourceFunctionalTest(Boolean enableDownload, String targetUrl, int expectedStatusCode) {
        this.enableDownload = enableDownload;
        this.targetUrl = targetUrl;
        this.expectedStatusCode = expectedStatusCode;
    }

    @Parameterized.Parameters(name = "{index}: with download enabled:{0} -> {1} returns {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
   //             { true, "/download", 200 },
     //           { true, "/download-register", 200 },
     //           { false, "/download", 200 },
                { false, "/download-register", 501 }
        });
    }

    @Test
    public void checkDownloadResourceStatusCode() throws Exception {
        TestRegister testRegister = enableDownload ? REGISTER_WITH_DOWNLOAD_ENABLED : REGISTER_WITH_DOWNLOAD_DISABLED;

        Response response = register.getRequest(testRegister, targetUrl);
        
        assertThat(response.getStatus(), equalTo(expectedStatusCode));
    }
}
