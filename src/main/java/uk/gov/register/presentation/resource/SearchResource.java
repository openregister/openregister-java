package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public class SearchResource {
    private final RecentEntryIndexQueryDAO queryDAO;
    @Context
    protected HttpServletRequest httpServletRequest;

    public SearchResource(RecentEntryIndexQueryDAO queryDAO) {
        this.queryDAO = queryDAO;
    }

//    private String getRegisterPrimaryKey() {
//        try {
//
//            String host = new URI(httpServletRequest.getRequestURL().toString()).getHost();
//
//            if (host.startsWith("localhost")) return "test-register";
//            else return host.replaceAll("([^\\.]+)\\.(openregister)\\..*", "$1");
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @GET
    @Path("/{primaryKey}/{primaryKeyValue}")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode findByPrimaryKey(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) {
        List<JsonNode> jsonNodes = queryDAO.find(key, value);
        return jsonNodes.isEmpty() ? null : jsonNodes.get(0);

    }
}
