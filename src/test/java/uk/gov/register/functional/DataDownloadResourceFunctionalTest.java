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
public class DataDownloadResourceFunctionalTest {

    @Rule
    public RegisterRule register;

    private final String targetUrl;
    private final int expectedStatusCode;

    public DataDownloadResourceFunctionalTest(Boolean enableDownload, String targetUrl, int expectedStatusCode) {
        this.register = createRegister(enableDownload);
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
        Response response = register.getRequest("register", targetUrl);
        
        assertThat(response.getStatus(), equalTo(expectedStatusCode));
    }

    private static RegisterRule createRegister(Boolean enableResourceDownload){
        return new RegisterRule("register",
                ConfigOverride.config("enableDownloadResource", enableResourceDownload.toString()));
    }
}
