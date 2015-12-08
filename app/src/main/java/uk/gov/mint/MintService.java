package uk.gov.mint;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/")
public class MintService {
    private final LoadHandler loadHandler;

    public MintService(LoadHandler loadHandler) {
        this.loadHandler = loadHandler;
    }

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @Path("/load")
    public Response load(String payload) {
        try {
            loadHandler.handle(payload);
            return Response.ok().build();
        } catch (EntryValidationException e) {
            return Response.status(400).entity(e.getMessage() + " Error entry: '" + e.getEntry().toString() + "'").build();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }
    }
}
