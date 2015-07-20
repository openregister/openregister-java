package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.view.ListResultView;
import uk.gov.register.presentation.view.SingleResultView;

import javax.ws.rs.*;
import javax.ws.rs.core.*;


@Path("/")
public class SearchResource extends ResourceBase {

    private final RecentEntryIndexQueryDAO queryDAO;

    public SearchResource(RecentEntryIndexQueryDAO queryDAO) {
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("search")
    public Response search(@Context UriInfo uriInfo) {
        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        return buildResponse(new ListResultView("/templates/entries.mustache",
                        queryParameters.entrySet()
                                .stream()
                                .findFirst()
                                .map(e -> queryDAO.findAllByKeyValue(e.getKey(), e.getValue().get(0)))
                                .orElseGet(() -> queryDAO.getAllEntries(getRegisterPrimaryKey(), ENTRY_LIMIT)))
        );
    }

    @GET
    @Path("/{primaryKey}/{primaryKeyValue}")
    public Response findByPrimaryKey(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) {
        String registerPrimaryKey = getRegisterPrimaryKey();
        if (key.equals(registerPrimaryKey)) {
            Optional<JsonNode> entry = queryDAO.findByKeyValue(key, value);
            return buildResponse(new SingleResultView("/templates/entry.mustache", entry.isPresent() ? entry.get() : null));
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByHash(@PathParam("hash") String hash) {
        Optional<JsonNode> entry = queryDAO.findByHash(hash);
        return buildResponse(new SingleResultView("/templates/entry.mustache", entry.isPresent() ? entry.get() : null));
    }
}
