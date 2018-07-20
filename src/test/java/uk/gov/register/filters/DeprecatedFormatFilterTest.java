package uk.gov.register.filters;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;

public class DeprecatedFormatFilterTest {
    private final HttpServletResponse mock = mock(HttpServletResponse.class);

    @Rule
    public ResourceTestRule rule = ResourceTestRule.builder().addResource(new DeprecatedFormatFilterTest.DummyResource())
            .addProvider(new DeprecatedFormatFilter(mock))
            .build();

    @Test
    public void jsonResponseShouldNotWarn() {
        Response response = rule.client().target("/json").request().get();

        verify(mock, never()).setHeader(eq("Link"), anyString());
        verify(mock, never()).setHeader(eq("Warning"), anyString());

        // It shouldn't set any headers via the ContainerResponseContext either.
        assertFalse(response.getStringHeaders().containsKey("Warning"));
        assertFalse(response.getStringHeaders().containsKey("Link"));
    }

    @Test
    public void yamlResponseShouldWarn() {
        rule.client().target("/yaml").request().get();

        verify(mock).setHeader("Link", "</yaml.json>; rel=\"alternate\"; type=\"application/json\"");
        verify(mock).setHeader("Warning", "299 - \"Miscellaneous Persistent Warning\" \"yaml is deprecated and will be removed. See \"Link\" header for a format to use instead.\"");
    }

    @Test
    public void existingLinkHeadersShouldBePreserved() {
        // Only headers set on the httpServletResponse are preserved, due to an implementation detail
        // of Jersey's ContainerResponseContext.
        when(mock.getHeader("Link")).thenReturn("<?page-index=2&page-size=100>; rel=\"next\"");

        Response response = rule.client().target("/yaml").request().get();

        verify(mock).setHeader("Link", "<?page-index=2&page-size=100>; rel=\"next\",</yaml.json>; rel=\"alternate\"; type=\"application/json\"");
    }

    @Path("/")
    public class DummyResource {
        @GET
        @Path("json")
        @Produces(MediaType.APPLICATION_JSON)
        public String json() {
            return "{}";
        }

        @GET
        @Path("yaml")
        @Produces(ExtraMediaType.TEXT_YAML)
        public String yaml() {
            return "{}";
        }
    }
}
