package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.presentation.dao.PGObjectFactory;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public class SearchResource {
    private final RecentEntryIndexQueryDAO queryDAO;

    public SearchResource(RecentEntryIndexQueryDAO queryDAO) {
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("/{primaryKey}/{primaryKeyValue}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode findByPrimaryKey(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) {
        List<JsonNode> jsonNodes = queryDAO.find(PGObjectFactory.jsonbObject(String.format("{\"%s\":\"%s\"}", key, value)));
        return jsonNodes.isEmpty() ? null : jsonNodes.get(0);

    }
}
