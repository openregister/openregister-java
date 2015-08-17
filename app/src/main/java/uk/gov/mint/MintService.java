package uk.gov.mint;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class MintService {
    private final LoadHandler loadHandler;

    public MintService(LoadHandler loadHandler) {
        this.loadHandler = loadHandler;
    }


    @POST
    @Path("/load")
    public Response load(String payload) {
        try {
            loadHandler.handle(payload);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }
    }
}
