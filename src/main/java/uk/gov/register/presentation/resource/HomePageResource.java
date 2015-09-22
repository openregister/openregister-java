package uk.gov.register.presentation.resource;

import io.dropwizard.views.View;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class HomePageResource {
    private final ViewFactory viewFactory;

    @Inject
    public HomePageResource(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    @GET
    @Produces({ExtraMediaType.TEXT_HTML})
    public View home() {
        return viewFactory.thymeleafView("home.html");
    }

    @GET
    @Path("/robots.txt")
    @Produces(MediaType.TEXT_PLAIN)
    public String robots() {
        return "User-agent: *\n" +
                "Disallow: /\n";
    }
}
