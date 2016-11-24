package uk.gov.register.functional;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class ApplicationTest extends FunctionalTestBase {
    @Test
    public void appSupportsCORS() {
        String origin = "http://originfortest.com";
        Response response = client.target("http://localhost:" + APPLICATION_PORT + "/entries?cors-test")
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
        Response response = client.target("http://localhost:" + APPLICATION_PORT + "/entries")
                .request()
                .get();

        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_SECURITY_POLICY), equalTo(ImmutableList.of("default-src 'self' www.google-analytics.com")));
    }

    @Test
    public void appExplicitlySendsHtmlCharsetInHeader() throws Exception {
        Response response = client.target("http://localhost:" + APPLICATION_PORT + "/entries")
                .request()
                .get();

        String contentType = (String) response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0);
        assertThat(contentType, containsString("text/html"));
        assertThat(contentType, containsString("charset=utf-8"));
    }

    @Test
    public void appSupportsContentTypeOptions() throws Exception {
        Response response = client.target("http://localhost:" + APPLICATION_PORT + "/entries")
                .request()
                .get();

        assertThat(response.getHeaders().get(HttpHeaders.X_CONTENT_TYPE_OPTIONS), equalTo(ImmutableList.of("nosniff")));
    }

    @Test
    public void appSupportsXssProtection() throws Exception {
        Response response = client.target("http://localhost:" + APPLICATION_PORT + "/entries")
                .request()
                .get();

        assertThat(response.getHeaders().get(HttpHeaders.X_XSS_PROTECTION), equalTo(ImmutableList.of("1; mode=block")));
    }

    @Test
    public void app404PageHasXhtmlLangAttributes() throws Exception {
        Response response = client.target("http://localhost:" + APPLICATION_PORT + "/missing_page_to_force_a_404")
                .request()
                .get();

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Elements htmlElement = doc.select("html");
        assertThat(htmlElement.size(), equalTo(1));
        assertThat(htmlElement.first().attr("lang"), equalTo("en"));
        assertThat(htmlElement.first().attr("xml:lang"), equalTo("en"));
    }
}
