package uk.gov.register.resources;

import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.api.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Path("/")
public class SearchResource {

    protected final RequestContext requestContext;
    private final String registerPrimaryKey;

    @Inject
    public SearchResource(RequestContext requestContext, RegisterNameConfiguration registerNameConfiguration) {
        this.requestContext = requestContext;
        registerPrimaryKey = registerNameConfiguration.getRegister();
    }

    @GET
    @Path("/{key}/{value}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public Object find(@PathParam("key") String key, @PathParam("value") String value) throws Exception {
        String redirectUrl = key.equals(registerPrimaryKey) ?
                String.format("/record/%s", encodeUrlValue(value)) :
                String.format("/records/%s/%s", key, encodeUrlValue(value));

        return Response.status(301).location(URI.create(redirectUrl)).build();
    }

    private String encodeUrlValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

}
