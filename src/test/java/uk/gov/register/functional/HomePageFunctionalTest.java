package uk.gov.register.functional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class HomePageFunctionalTest {

    private static final String REGISTER_WITH_COPYRIGHT_FIELD = "postcode";

    @ClassRule
    public static final RegisterRule register = new RegisterRule();

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
    public void homepageHasCopyrightInMainBodyRenderedAsMarkdown() throws Throwable {
        // assumes that registers.yaml has a `postcode` entry with a copyright field containing markdown links
        // might be good to find a way to specify this in the test
        Response response = register.getRequest(REGISTER_WITH_COPYRIGHT_FIELD, "/");
        Document doc = Jsoup.parse(response.readEntity(String.class));

        Elements copyrightParagraph = doc.select("main .registry-copyright");
        Elements links = copyrightParagraph.select("a");
        assertThat(links.size(), greaterThan(0));
    }

    @Test
    public void homepageHasCopyrightInFooterRenderedAsMarkdown() throws Throwable {
        // assumes that registers.yaml has a `postcode` entry with a copyright field containing markdown links
        // might be good to find a way to specify this in the test
        Response response = register.getRequest(REGISTER_WITH_COPYRIGHT_FIELD, "/");
        Document doc = Jsoup.parse(response.readEntity(String.class));

        Elements copyrightParagraph = doc.select("footer .registry-copyright");
        Elements links = copyrightParagraph.select("a");
        assertThat(links.size(), greaterThan(0));
    }
}
