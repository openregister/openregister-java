package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.view.ResultView;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/")
public class SearchResource extends ResourceBase {

    private final RecentEntryIndexQueryDAO queryDAO;

    public SearchResource(RecentEntryIndexQueryDAO queryDAO) {
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("/{primaryKey}/{primaryKeyValue}")
    public Response findByPrimaryKey(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) {
        String registerPrimaryKey = getRegisterPrimaryKey();
        if (key.equals(registerPrimaryKey)) {
            Optional<JsonNode> entry = queryDAO.findByKeyValue(key, value);
            return buildResponse(new ResultView("/templates/entry.mustache", entry.isPresent() ? entry.get() : null));
        }

        throw new BadRequestException("Key: " + key + " is not primary key of the register.");
    }

    @GET
    @Path("/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByHash(@PathParam("hash") String hash) {
        Optional<JsonNode> entry = queryDAO.findByHash(hash);
        return buildResponse(new ResultView("/templates/entry.mustache", entry.isPresent() ? entry.get() : null));
    }
}