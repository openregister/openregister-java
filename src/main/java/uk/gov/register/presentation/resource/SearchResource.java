package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.net.URI;

@Path("/")
public class SearchResource {
    private final RecentEntryIndexQueryDAO queryDAO;
    @Context
    protected HttpServletRequest httpServletRequest;

    public SearchResource(RecentEntryIndexQueryDAO queryDAO) {
        this.queryDAO = queryDAO;
    }


    //Note: copied the logic to fetch primary key from alpha register.
    //Note: We might need to change the logic of extracting register primary key for beta registers
    private String getRegisterPrimaryKey() {
        try {

            String host = new URI(httpServletRequest.getRequestURL().toString()).getHost();

            //hack for functional tests
            if (host.startsWith("localhost")) return "ft_test_pkey";
            else return host.replaceAll("([^\\.]+)\\.(openregister)\\..*", "$1");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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