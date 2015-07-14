package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public class DataResource extends ResourceBase{
    public static final int ENTRY_LIMIT = 100;
    private final RecentEntryIndexQueryDAO queryDAO;

    public DataResource(RecentEntryIndexQueryDAO queryDAO) {
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("/feed")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<JsonNode> feed() {
        return queryDAO.getFeeds(ENTRY_LIMIT);
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<JsonNode> all() {
        return queryDAO.getAllEntries(getRegisterPrimaryKey(), ENTRY_LIMIT);
    }

}
