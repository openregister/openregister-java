package uk.gov.register.resources;

import io.dropwizard.views.View;
import org.flywaydb.core.Flyway;
import uk.gov.register.configuration.RegisterTrackingConfiguration;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;

@Path("/")
public class HomePageResource {
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;
    private final RegisterTrackingConfiguration config;
    private Flyway flyway;

    @Inject
    public HomePageResource(RegisterReadOnly register, ViewFactory viewFactory, RegisterTrackingConfiguration config, Flyway flyway) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.config = config;
        this.flyway = flyway;
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

    @DELETE
    @PermitAll
    @Path("/delete-register-data")
    @DataDeleteNotAllowed
    public Response deleteRegisterData() {
        flyway.clean();
        flyway.setBaselineVersionAsString("0");
        flyway.migrate();
        return Response.status(200).entity("Data has been deleted").build();
    }
}
