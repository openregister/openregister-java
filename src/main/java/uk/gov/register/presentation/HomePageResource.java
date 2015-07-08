package uk.gov.register.presentation;

import io.dropwizard.views.View;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class HomePageResource {
    @GET
    @Produces({MediaType.TEXT_HTML})
    public View home() {
        return new View("/templates/home.mustache") {
        };
    }
}
