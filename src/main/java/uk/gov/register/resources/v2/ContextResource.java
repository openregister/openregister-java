package uk.gov.register.resources.v2;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.views.v2.ContextView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/next/context")
public class ContextResource {
    private final RegisterContext registerContext;

    @Inject
    public ContextResource(RegisterContext registerContext) {
        this.registerContext = registerContext;
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public ContextView getContext() {
        return new ContextView(registerContext);
    }
}
