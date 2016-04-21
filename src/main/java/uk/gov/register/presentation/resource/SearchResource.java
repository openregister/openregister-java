package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;


@Path("/")
public class SearchResource {

    protected final RequestContext requestContext;

    @Inject
    public SearchResource(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @GET
    @Path("/{key}/{value}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public Object find(@PathParam("key") String key, @PathParam("value") String value) throws Exception {
        String redirectUrl = key.equals(requestContext.getRegisterPrimaryKey()) ?
                String.format("/record/%s", value) :
                String.format("/records/%s/%s", key, value);

        return Response.status(301).location(URI.create(redirectUrl)).build();
    }
}
