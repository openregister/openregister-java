package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * This resource the latest versions of the requested key,
 * i.e. non-superseded versions only.
 */
@Path("/")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class AllResource {
    public static final int ENTRY_LIMIT = 100;
    private final String name;
    private final RecentEntryIndexQueryDAO queryDAO;

    public AllResource(String name, RecentEntryIndexQueryDAO queryDAO) {
        this.name = name;
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<JsonNode> get() {
        return queryDAO.getAllEntries(name, ENTRY_LIMIT);
    }
}
