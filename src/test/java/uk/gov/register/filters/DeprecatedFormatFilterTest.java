package uk.gov.register.filters;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class DeprecatedFormatFilterTest {
    @Rule
    public ResourceTestRule rule = ResourceTestRule.builder().addResource(new DeprecatedFormatFilterTest.DummyResource())
            .addProvider(new DeprecatedFormatFilter())
            .build();

    @Test
    public void jsonResponseShouldNotWarn() {
        Response response = rule.client().target("/json").request().get();

        assertFalse(response.getStringHeaders().containsKey("Warning"));
        assertFalse(response.getStringHeaders().containsKey("Link"));
    }

    @Test
    public void yamlResponseShouldWarn() {
        Response response = rule.client().target("/yaml").request().get();

        String warning = response.getHeaderString("Warning");
        String link = response.getHeaderString("Link");

        assertThat(warning, equalTo("299 - \"Miscellaneous Persistent Warning\" \"yaml is deprecated and will be removed. See \"Link\" header for a format to use instead.\""));
        assertThat(link, equalTo("/yaml.json; rel=\"alternate\"; type=\"application/json\""));
    }

    @Test
    public void existingLinkHeadersShouldBePreserved() {
        Response response = rule.client().target("/existing-header").request().get();

        String link = response.getHeaderString("Link");

        assertThat(link, equalTo("<?page-index=2&page-size=100>; rel=\"next\",/existing-header.json; rel=\"alternate\"; type=\"application/json\""));
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

        @GET
        @Path("existing-header")
        public Response existingHeader() {
            return Response
                    .status(200)
                    .header("Link", "<?page-index=2&page-size=100>; rel=\"next\"")
                    .type(ExtraMediaType.TEXT_YAML)
                    .entity("{}")
                    .build();
        }
    }
}
