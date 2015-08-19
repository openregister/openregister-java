package uk.gov.register.presentation.resource;

import io.dropwizard.views.View;
import thymeleaf.ThymeleafView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class HomePageResource extends ResourceBase{
    @GET
    @Produces({MediaType.TEXT_HTML})
    public View home() {
        return new ThymeleafView(httpServletRequest, httpServletResponse, servletContext, "home.html") {
        };
    }
}
