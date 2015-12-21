package uk.gov.mint;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.List;

@Path("/")
public class MintService {
    private final List<Handler> loadHandlers;

    public MintService(List<Handler> loadHandlers) {
        this.loadHandlers = loadHandlers;
    }

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @PermitAll
    @Path("/load")
    public void load(String payload) {
        for (Handler h : loadHandlers) {
            h.handle(payload);
        }
    }
}
