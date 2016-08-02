package uk.gov.register.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.register.configuration.ResourceConfiguration;
import uk.gov.register.filters.ResourceAvailabilityFilter;
import uk.gov.register.views.ViewFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceAvailabilityFilterTest {

    @Rule
    public ResourceTestRule resourceNotAvailableRule = ResourceTestRule.builder().addResource(new DummyResource()).addProvider(getResourceAvailabilityFilter(false)).build();
    @Rule
    public ResourceTestRule resourceAvailableRule = ResourceTestRule.builder().addResource(new DummyResource()).addProvider(getResourceAvailabilityFilter(true)).build();

    @Test
    public void shouldReturn501WhenEnableDownloadSetToFalse() {
        Response response = resourceNotAvailableRule.client().target("/test").request().get();
        assertThat(response.getStatus(), equalTo(501));
    }

    @Test
    public void shouldReturn200WhenEnableDownloadSetToTrue() {
        Response response = resourceAvailableRule.client().target("/test").request().get();
        assertThat(response.getStatus(), equalTo(200));
    }

    @Path("/")
    public class DummyResource {
        @GET
        @Path("test")
        @DownloadNotAvailable
        public String getPrincipal() {
            return "success";
        }
    }

    private ResourceAvailabilityFilter getResourceAvailabilityFilter(boolean available) {
        ViewFactory viewFactory = mock(ViewFactory.class);
        ResourceConfiguration resourceConfiguration = mock(ResourceConfiguration.class);
        when(resourceConfiguration.getEnableDownloadResource()).thenReturn(available);
        return new ResourceAvailabilityFilter(resourceConfiguration, viewFactory);
    }
}
