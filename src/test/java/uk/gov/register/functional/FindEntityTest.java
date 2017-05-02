package uk.gov.register.functional;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.views.representations.ExtraMediaType.TEXT_HTML;

public class FindEntityTest {

    @ClassRule
    public static RegisterRule register = new RegisterRule();

    @Before
    public void publishTestMessages() {
        register.wipe();
        register.mintLines(address, "{\"street\":\"ellis\",\"address\":\"12345\"}", "{\"street\":\"presley\",\"address\":\"6789\"}", "{\"street\":\"ellis\",\"address\":\"145678\"}");
    }

    @Test
    public void shouldRedirectCorrectly_whenUsingOldUrlForFindByPrimaryKey() throws JSONException {
        WebTarget target = register.target(address);
        target.property("jersey.config.client.followRedirects",false);
        Response response = target.path("/address/12345.json").request().get();

        assertThat(response.getStatus(), equalTo(301));
        String expectedRedirect = "/record/12345";
        URI location = URI.create(response.getHeaderString("Location"));
        assertThat(location.getPath(), equalTo(expectedRedirect));
    }

    @Test
    public void find_returnsTheCorrectTotalRecordsInPaginationHeader() {
        Response response = register.getRequest(address, "/records/street/ellis", TEXT_HTML);

        Document doc = Jsoup.parse(response.readEntity(String.class));

        assertThat(doc.body().getElementById("main").getElementsByAttributeValue("class", "heading-secondary").first().text(), equalTo("2 records"));
    }
}
