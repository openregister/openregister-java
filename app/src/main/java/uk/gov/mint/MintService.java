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
            loadHandler.handle(extractRegisterNameFromRequest(), payload);
            return Response.ok().build();
        } catch (Exception e) {
            if (e.getCause() instanceof EntryValidator.EntryValidationException) {
                return Response.status(400).entity(e).build();
            } else {
                return Response.serverError().entity(e).build();
            }

        }
    }

    protected String extractRegisterNameFromRequest() {
        String host = httpServletRequest.getHeader("Host");
        return host.split(":")[0].split("\\.")[0];
    }
}
