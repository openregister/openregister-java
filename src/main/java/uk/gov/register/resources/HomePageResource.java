package uk.gov.register.resources;

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
import java.security.NoSuchAlgorithmException;

@Path("/")
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
    public View home() throws NoSuchAlgorithmException {
        return viewFactory.homePageView(
                register.getTotalRecords(),
                register.getTotalEntries(),
                register.getLastUpdatedTime()
        );
    }

    @GET
    @Path("/robots.txt")
    @Produces(MediaType.TEXT_PLAIN)
    public String robots() {
        return "User-agent: *\n" +
                "Disallow: /\n";
    }
    @GET
    @Path("/analytics-code.js")
    @Produces(ExtraMediaType.APPLICATION_JAVASCRIPT)
    public String analyticsTrackingId() {
        return config.getRegisterTrackingId().map(
                trackingId -> "var gaTrackingId = \"" + trackingId + "\";\n"
        ).orElse("");
    }
}
