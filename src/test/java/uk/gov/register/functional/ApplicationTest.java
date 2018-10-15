package uk.gov.register.functional;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.views.representations.ExtraMediaType.TEXT_HTML;

public class ApplicationTest {
    @ClassRule
    public static RegisterRule register = new RegisterRule();

    @Before
    public void setup() {
        register.wipe();
        register.loadRsf(address, RsfRegisterDefinition.ADDRESS_FIELDS + RsfRegisterDefinition.ADDRESS_REGISTER);
    }

    @Test
    public void appSupportsCORS() {
        String origin = "http://originfortest.com";
        Response response = register.target(address).path("/entries")
                .request()
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "X-Requested-With")
                .options();

        MultivaluedMap<String, Object> headers = response.getHeaders();

        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), equalTo(ImmutableList.of(origin)));
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), is(nullValue()));
        assertNotNull(headers.get(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS), equalTo(ImmutableList.of("GET,HEAD")));
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS), equalTo(ImmutableList.of("X-Requested-With,Content-Type,Accept,Origin")));
    }

    @Test
    public void appSupportsContentSecurityPolicy() throws Exception {
        Response response = register.getRequest(address, "/entries");

        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_SECURITY_POLICY), equalTo(ImmutableList.of("default-src 'self' www.google-analytics.com; connect-src 'self' www.google-analytics.com; img-src 'self' www.google-analytics.com; script-src 'self' www.google-analytics.com; font-src data:;")));
    }

    @Test
    public void appExplicitlySendsHtmlCharsetInHeader() throws Exception {
        Response response = register.getRequest(address, "/entries", TEXT_HTML);

        String contentType = (String) response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0);
        assertThat(contentType, containsString("text/html"));
        assertThat(contentType, containsString("charset=utf-8"));
    }

    @Test
    public void appSupportsContentTypeOptions() throws Exception {
        Response response = register.getRequest(address, "/entries");

        assertThat(response.getHeaders().get(HttpHeaders.X_CONTENT_TYPE_OPTIONS), equalTo(ImmutableList.of("nosniff")));
    }

    @Test
    public void appSupportsXssProtection() throws Exception {
        Response response = register.getRequest(address, "/entries");

        assertThat(response.getHeaders().get(HttpHeaders.X_XSS_PROTECTION), equalTo(ImmutableList.of("1; mode=block")));
    }

    @Test
    public void app404PageHasXhtmlLangAttributes() throws Exception {
        Response response = register.getRequest(address, "/missing_page_to_force_a_404");

        assertThat(response.getStatus(), equalTo(404));
        Document doc = Jsoup.parse(response.readEntity(String.class));
        Elements htmlElement = doc.select("html");
        assertThat(htmlElement.size(), equalTo(1));
        assertThat(htmlElement.first().attr("lang"), equalTo("en"));
        assertThat(htmlElement.first().attr("xml:lang"), equalTo("en"));
    }

    @Test
    public void returns405ForNotAllowedMethod() {
        Response response = register.getRequest(address, "/load-rsf");

        assertThat(response.getStatus(), equalTo(405));
    }

    @Test
    public void returns400BadRequest() {
        Response response = register.loadRsf(address, "nope");

        assertThat(response.getStatus(), equalTo(400));
    }

}
