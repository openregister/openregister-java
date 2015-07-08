package uk.gov.register.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.views.View;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public class PresentationResource {
    public static final int ENTRY_LIMIT = 100;
    private final RecentEntryIndexQueryDAO queryDAO;

    public PresentationResource(RecentEntryIndexQueryDAO queryDAO) {
        this.queryDAO = queryDAO;
    }

    @Path("/latest")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<JsonNode> get() {
        return queryDAO.getLatestEntries(ENTRY_LIMIT);
    }

    @GET
    @Path("/")
    @Produces({MediaType.TEXT_HTML})
    public View home(){
        return new View("/templates/home.mustache"){};
    }

}
