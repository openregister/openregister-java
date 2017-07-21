package uk.gov.register.functional;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;

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
        Response r = register.loadRsf(address, RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER +
            "add-item\t{\"address\":\"12345\",\"street\":\"ellis\"}\n" +
            "append-entry\tuser\t12345\t2017-06-13T09:20:41Z\tsha-256:19205fafe65406b9b27fce1b689abc776df4ddcf150c28b29b73b4ea054af6b9\n" +
            "add-item\t{\"address\":\"6789\",\"street\":\"presley\"}\n" +
            "append-entry\tuser\t6789\t2017-06-13T09:20:41Z\tsha-256:bd239db51960376826b937a615f0f3397485f00611d35bb7e951e357bf73b934\n" +
            "add-item\t{\"address\":\"145678\",\"street\":\"ellis\"}\n" +
            "append-entry\tuser\t145678\t2017-06-13T09:20:41Z\tsha-256:8ac926428ee49fb83c02bdd2556e62e84cfd9e636cd35eb1306ac8cb661e4983");
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
