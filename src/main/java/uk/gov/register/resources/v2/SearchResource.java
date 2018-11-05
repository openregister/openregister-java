package uk.gov.register.resources.v2;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Path("/next/")
public class SearchResource {

    private final RegisterId registerPrimaryKey;
    private final RegisterReadOnly register;

    @Inject
    public SearchResource(RegisterId registerId, RegisterReadOnly register) {
        registerPrimaryKey = registerId;
        this.register = register;
    }

    @GET
    @Path("/{key}/{value}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    @Timed
    public Object find(@PathParam("key") String key, @PathParam("value") String value) throws Exception {
        if (!key.equals(registerPrimaryKey.value()) && !register.getRegisterMetadata().getFields().contains(key)) {
            throw new NotFoundException();
        }

        String redirectUrl = key.equals(registerPrimaryKey.value()) ?
                String.format("/record/%s", encodeUrlValue(value)) :
                String.format("/records/%s/%s", key, encodeUrlValue(value));

        return Response.status(301).location(URI.create(redirectUrl)).build();
    }

    private String encodeUrlValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
