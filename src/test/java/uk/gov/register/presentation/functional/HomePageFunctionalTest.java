package uk.gov.register.presentation.functional;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import uk.gov.functional.CleanDatabaseRule;
import uk.gov.register.RegisterApplication;
import uk.gov.register.RegisterConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static uk.gov.functional.TestDBSupport.postgresConnectionString;

public class HomePageFunctionalTest  {

    private final String registerName = "postcode";

    private final DropwizardAppRule<RegisterConfiguration> appRule = new DropwizardAppRule<>(RegisterApplication.class,
            ResourceHelpers.resourceFilePath("test-app-config.yaml"),
            ConfigOverride.config("database.url", postgresConnectionString),
            ConfigOverride.config("jerseyClient.timeout", "3000ms"),
            ConfigOverride.config("register", registerName));

    @Rule
    public TestRule ruleChain = RuleChain.
            outerRule(new CleanDatabaseRule()).
            around(appRule);

    private Client client;

    @Before
    public void beforeEach(){
        client = createTestClient();
    }

    @Test
    public void homePageIsAvailableWhenNoDataInRegister() {
        Response response = getRequest("/");
        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void homepageHasXhtmlLangAttributes() throws Throwable {
        Response response = getRequest("/");

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Elements htmlElement = doc.select("html");
        assertThat(htmlElement.size(), equalTo(1));
        assertThat(htmlElement.first().attr("lang"), equalTo("en"));
        assertThat(htmlElement.first().attr("xml:lang"), equalTo("en"));
    }

    @Test
    public void homepageHasCopyrightInMainBodyRenderedAsMarkdown() throws Throwable {
        // assumes that registers.yaml has a `postcode` entry with a copyright field containing markdown links
        // might be good to find a way to specify this in the test
        Response response = getRequest("/");
        Document doc = Jsoup.parse(response.readEntity(String.class));

        Elements copyrightParagraph = doc.select("main .registry-copyright");
        Elements links = copyrightParagraph.select("a");
        assertThat(links.size(), greaterThan(0));
    }

    @Test
    public void homepageHasCopyrightInFooterRenderedAsMarkdown() throws Throwable {
        // assumes that registers.yaml has a `postcode` entry with a copyright field containing markdown links
        // might be good to find a way to specify this in the test
        Response response = getRequest("/");
        Document doc = Jsoup.parse(response.readEntity(String.class));

        Elements copyrightParagraph = doc.select("footer .registry-copyright");
        Elements links = copyrightParagraph.select("a");
        assertThat(links.size(), greaterThan(0));
    }

    private Response getRequest(String path) {
        return client.target(String.format("http://localhost:%d%s", appRule.getLocalPort(), path)).request().get();
    }

    private Client createTestClient() {
        return new io.dropwizard.client.JerseyClientBuilder(appRule.getEnvironment())
                .using(appRule.getConfiguration().getJerseyClientConfiguration())
                .build("test client");
    }
}
