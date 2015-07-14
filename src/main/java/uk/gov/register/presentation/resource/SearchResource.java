package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public class SearchResource extends ResourceBase {
    private final RecentEntryIndexQueryDAO queryDAO;

    public SearchResource(RecentEntryIndexQueryDAO queryDAO) {
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("/{primaryKey}/{primaryKeyValue}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode findByPrimaryKey(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) {
        String registerPrimaryKey = getRegisterPrimaryKey();
        if (key.equals(registerPrimaryKey)) {
            Optional<JsonNode> entry = queryDAO.findByKeyValue(key, value);
            return entry.isPresent() ? entry.get() : null;
        }

        throw new BadRequestException("Key: " + key + " is not primary key of the register.");
    }

    @GET
    @Path("/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode findByHash(@PathParam("hash") String hash) {
        Optional<JsonNode> entry = queryDAO.findByHash(hash);
        return entry.isPresent() ? entry.get() : null;
    }

}
