package uk.gov.register.presentation.functional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import uk.gov.register.presentation.functional.testSupport.DBSupport;

import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HomePageFunctionalTest extends FunctionalTestBase {
    @Test
    public void homePageIsAvailableWhenNoDataInRegister() {
        Response response = getRequest("/");
        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void homePageIsAvailableWhenLatestEntryHasNullLeafInput() throws Throwable {
        DBSupport.publishMessagesWithoutLeafInput("address", Collections.singletonList("{\"address\":\"1234\"}"));
        Response response = getRequest("/");
        assertThat(response.getStatus(), equalTo(200));
        cleanDatabaseRule.before();
    }

    @Test
    public void homepageHasXhtmlLangAttributes() throws Throwable {
        DBSupport.publishMessagesWithoutLeafInput("address", Collections.singletonList("{\"address\":\"1234\"}"));
        Response response = getRequest("/");

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Elements htmlElement = doc.select("html");
        assertThat(htmlElement.size(), equalTo(1));
        assertThat(htmlElement.first().attr("lang"), equalTo("en"));
        assertThat(htmlElement.first().attr("xml:lang"), equalTo("en"));

        cleanDatabaseRule.before();
    }
}
