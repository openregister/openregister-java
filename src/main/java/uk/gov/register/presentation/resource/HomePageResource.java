package uk.gov.register.presentation.resource;

import io.dropwizard.views.View;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class HomePageResource extends ResourceBase{
    @Inject
    public HomePageResource(RequestContext requestContext) {
        super(requestContext);
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    public View home() {
        return new ThymeleafView(requestContext, "home.html");
    }

    @GET
    @Path("/robots.txt")
    @Produces(MediaType.TEXT_PLAIN)
    public String robots() {
        return "User-agent: *\n" +
                "Disallow: /\n";
    }
}
