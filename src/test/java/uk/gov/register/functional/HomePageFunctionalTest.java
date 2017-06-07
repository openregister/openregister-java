package uk.gov.register.functional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;
import uk.gov.register.functional.app.TestRegister;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.functional.app.TestRegister.postcode;

public class HomePageFunctionalTest {

    private static final TestRegister REGISTER_WITH_COPYRIGHT_FIELD = postcode;

    @ClassRule
    public static final RegisterRule register = new RegisterRule();

    @Before
    public void setup() {
        register.loadRsf(postcode, RsfRegisterDefinition.POSTCODE_REGISTER);
    }

    @Test
    public void homePageIsAvailableWhenNoDataInRegister() {
        Response response = register.getRequest(REGISTER_WITH_COPYRIGHT_FIELD, "/");
        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void homepageHasXhtmlLangAttributes() throws Throwable {
        Response response = register.getRequest(REGISTER_WITH_COPYRIGHT_FIELD, "/");

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Elements htmlElement = doc.select("html");
        assertThat(htmlElement.size(), equalTo(1));
        assertThat(htmlElement.first().attr("lang"), equalTo("en"));
        assertThat(htmlElement.first().attr("xml:lang"), equalTo("en"));
    }

    @Test
    public void homepageHasCopyrightInFooterRenderedAsMarkdown() throws Throwable {
        // assumes that registers.yaml has a `postcode` entry with a copyright field containing markdown links
        // might be good to find a way to specify this in the test
        Response response = register.getRequest(REGISTER_WITH_COPYRIGHT_FIELD, "/");
        Document doc = Jsoup.parse(response.readEntity(String.class));

        Elements copyrightParagraph = doc.select("footer .copyright");
        Elements links = copyrightParagraph.select("a");
        assertThat(links.size(), greaterThan(0));
    }
}
