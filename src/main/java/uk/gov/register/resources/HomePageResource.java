package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.views.View;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Timed
public class HomePageResource {
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;
    private final RegisterTrackingConfiguration config;

    @Inject
    public HomePageResource(RegisterReadOnly register, ViewFactory viewFactory, RegisterTrackingConfiguration config) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.config = config;
    }

    @GET
    @Produces({ExtraMediaType.TEXT_HTML})
    @Timed
    public View home() {
        return viewFactory.homePageView(
                register.getTotalRecords(),
                register.getTotalEntries(),
                register.getLastUpdatedTime(),
                register.getCustodianName()
        );
    }

    @GET
    @Path("/robots.txt")
    @Produces(MediaType.TEXT_PLAIN)
    @Timed
    public String robots() {
        return "User-agent: *\n" +
                "Disallow: /\n";
    }

    @GET
    @Path("/analytics-id.js")
    @Produces(ExtraMediaType.APPLICATION_JAVASCRIPT)
    @Timed
    public String analyticsTrackingId() {
        return config.getRegisterTrackingId().map(
                trackingId -> "var gaTrackingId = \"" + trackingId + "\";\n"
        ).orElse("");
    }
}
