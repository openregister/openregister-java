package uk.gov.register.functional;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.register.RegisterApplication;
import uk.gov.register.RegisterConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.register.functional.db.TestDBSupport.postgresConnectionString;

@RunWith(Parameterized.class)
public class DataDownloadResourceFunctionalTest {

    @Rule
    public DropwizardAppRule<RegisterConfiguration> appRule;

    private final String targetUrl;
    private final int expectedStatusCode;

    public DataDownloadResourceFunctionalTest(Boolean enableDownload, String targetUrl, int expectedStatusCode) {
        this.appRule = createAppRule(enableDownload);
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
        Client client = getTestClient(this.appRule);
        Response response = client.target(String.format("http://localhost:%d%s", this.appRule.getLocalPort(), targetUrl))
                .request().get();
        
        assertThat(response.getStatus(), equalTo(expectedStatusCode));
    }

    private Client getTestClient(DropwizardAppRule<RegisterConfiguration> appRule) {
        return new io.dropwizard.client.JerseyClientBuilder(appRule.getEnvironment())
                .using(appRule.getConfiguration().getJerseyClientConfiguration())
                .build("test client");
    }

    private static DropwizardAppRule<RegisterConfiguration> createAppRule(Boolean enableResourceDownload){
        return new DropwizardAppRule<>(RegisterApplication.class,
                ResourceHelpers.resourceFilePath("test-app-config.yaml"),
                ConfigOverride.config("database.url", postgresConnectionString),
                ConfigOverride.config("jerseyClient.timeout", "3000ms"),
                ConfigOverride.config("register", "register"),
                ConfigOverride.config("enableDownloadResource", enableResourceDownload.toString()));
    }
}
