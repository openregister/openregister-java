package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.view.ResultView;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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

        if (queryParameters.size() > 1) {
            JsonNode node = JsonObjectMapper.convertToJsonNode("{\"entry\": {\"error_message\": \"Cannot search on multiple keys currently\"}}".getBytes());
            return buildResponse(new ResultView("/templates/entry.mustache", node));
        }

        Optional<Pair<String, String>> queryCriteria = getFirstKeyValuePair(queryParameters);
        Optional<JsonNode> entry;
        if (queryCriteria.isPresent()) {
            Pair<String, String> p = queryCriteria.get();
            entry = queryDAO.findByKeyValue(p.key, p.value);
        } else {
            entry = Optional.absent();
        }

        return buildResponse(new ResultView("/templates/entry.mustache", entry.isPresent() ? entry.get() : null));
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

    private Optional<Pair<String, String>> getFirstKeyValuePair(MultivaluedMap<String, String> m) {
        Iterator<Map.Entry<String, List<String>>> paramsIterator = m.entrySet().iterator();
        Optional<Pair<String, String>> entry;
        if (paramsIterator.hasNext()) {
            Map.Entry<String, List<String>> queryParam = paramsIterator.next();
            if (queryParam != null) {
                entry = Optional.of(new Pair(queryParam.getKey(), queryParam.getValue().get(0)));
            } else {
                entry = Optional.absent();
            }
        } else {
            entry = Optional.absent();
        }

        return entry;
    }

}
