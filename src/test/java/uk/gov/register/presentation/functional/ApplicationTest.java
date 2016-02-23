package uk.gov.register.presentation.functional;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class ApplicationTest extends FunctionalTestBase {
    @Test
    public void appSupportsCORS() {
        String origin = "http://originfortest.com";
        Response response = client.target("http://address.openregister.dev:" + APPLICATION_PORT + "/entries")
                .request()
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "X-Requested-With")
                .options();


        MultivaluedMap<String, Object> headers = response.getHeaders();

        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), equalTo(ImmutableList.of(origin)));
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), equalTo(ImmutableList.of("true")));
        assertNotNull(headers.get(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS), equalTo(ImmutableList.of("OPTIONS,GET,PUT,POST,DELETE,HEAD")));
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS), equalTo(ImmutableList.of("X-Requested-With,Content-Type,Accept,Origin")));
    }

    @Test
    public void appSupportsContentSecurityPolicy() throws Exception {
        Response response = client.target("http://address.openregister.dev:" + APPLICATION_PORT + "/entries")
                .request()
                .get();

        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_SECURITY_POLICY), equalTo(ImmutableList.of("default-src 'self'")));
    }

    @Test
    public void appExplicitlySendsHtmlCharsetInHeader() throws Exception {
        Response response = client.target("http://address.openregister.dev:" + APPLICATION_PORT + "/entries")
                .request()
                .get();

        String contentType = (String) response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0);
        assertThat(contentType, containsString("text/html"));
        assertThat(contentType, containsString("charset=UTF-8"));
    }

    @Test
    public void appSupportsContentTypeOptions() throws Exception {
        Response response = client.target("http://address.openregister.dev:" + APPLICATION_PORT + "/entries")
                .request()
                .get();

        assertThat(response.getHeaders().get(HttpHeaders.X_CONTENT_TYPE_OPTIONS), equalTo(ImmutableList.of("nosniff")));
    }

    @Test
    public void appSupportsXssProtection() throws Exception {
        Response response = client.target("http://address.openregister.dev:" + APPLICATION_PORT + "/entries")
                .request()
                .get();

        assertThat(response.getHeaders().get(HttpHeaders.X_XSS_PROTECTION), equalTo(ImmutableList.of("1; mode=block")));
    }

}
