package uk.gov.mint;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/")
public class MintService {
    private final String register;
    private final LoadHandler loadHandler;

    public MintService(String register, LoadHandler loadHandler) {
        this.register = register;
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
        } catch (EntryValidationException e) {
            return Response.status(400).entity(e.getMessage() + " Error entry: '" + e.getEntry().toString() + "'").build();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }
    }

    protected String extractRegisterNameFromRequest() {
        if(StringUtils.isNotBlank(register)){
            return register;
        }
        String host = httpServletRequest.getHeader("Host");
        return host.split(":")[0].split("\\.")[0];
    }
}
