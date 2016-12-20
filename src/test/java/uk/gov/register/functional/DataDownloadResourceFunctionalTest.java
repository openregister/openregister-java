package uk.gov.register.functional;

import org.junit.ClassRule;
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
public class DataDownloadResourceFunctionalTest {

    public static final String REGISTER_WITH_DOWNLOAD_ENABLED = "address";
    public static final String REGISTER_WITH_DOWNLOAD_DISABLED = "register";
    @ClassRule
    public static RegisterRule register = new RegisterRule();

    private final String targetUrl;
    private final int expectedStatusCode;
    private final Boolean enableDownload;

    public DataDownloadResourceFunctionalTest(Boolean enableDownload, String targetUrl, int expectedStatusCode) {
        this.enableDownload = enableDownload;
        this.targetUrl = targetUrl;
        this.expectedStatusCode = expectedStatusCode;
    }

    @Parameterized.Parameters(name = "{index}: with download enabled:{0} -> {1} returns {2}")
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][] {
                { true, "/download", 200 },
                { true, "/download-register", 200 },
                { false, "/download", 200 },
                { false, "/download-register", 501 }
        });
    }

    @Test
    public void checkDownloadResourceStatusCode() throws Exception {
        String registerName = enableDownload ? REGISTER_WITH_DOWNLOAD_ENABLED : REGISTER_WITH_DOWNLOAD_DISABLED;

        Response response = register.getRequest(registerName, targetUrl);
        
        assertThat(response.getStatus(), equalTo(expectedStatusCode));
    }
}
