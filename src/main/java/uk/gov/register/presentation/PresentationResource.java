package uk.gov.register.presentation;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/latest")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class PresentationResource {
    public static final int ENTRY_LIMIT = 100;
    private final RecentEntryIndexQueryDAO queryDAO;

    public PresentationResource(RecentEntryIndexQueryDAO queryDAO) {
        this.queryDAO = queryDAO;
    }

    @GET
    public List<JsonNode> get() {
        return queryDAO.getLatestEntries(ENTRY_LIMIT);
    }
}
