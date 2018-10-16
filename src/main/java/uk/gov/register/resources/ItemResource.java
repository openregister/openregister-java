package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.Blob;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.BlobView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

@Deprecated
@Path("/items")
public class ItemResource {
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;

    @Inject
    public ItemResource(RegisterReadOnly register, ViewFactory viewFactory) {
        this.register = register;
        this.viewFactory = viewFactory;
    }

    @GET
    @Path("/sha-256:{item-hash}")
    @Produces(ExtraMediaType.TEXT_HTML)
    public Response getItemWebViewByHex(@PathParam("item-hash") String blobHash) {
        URI location = UriBuilder.fromResource(BlobResource.class).path(BlobResource.class, "getBlobWebViewByHex").build(blobHash);
        return Response.status(Response.Status.MOVED_PERMANENTLY).location(location).build();
    }

    @GET
    @Path("/sha-256:{item-hash}")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_YAML,
            ExtraMediaType.TEXT_TTL,
            ExtraMediaType.TEXT_CSV,
            ExtraMediaType.TEXT_TSV,
            ExtraMediaType.APPLICATION_SPREADSHEET
    })
    @Timed
    public BlobView getItemDataByHex(@PathParam("item-hash") String itemHash) throws FieldConversionException {
        return getItem(itemHash).map(viewFactory::getBlobMediaView)
                .orElseThrow(() -> new NotFoundException("No item found with item hash: " + itemHash));
    }

    private Optional<Blob> getItem(String itemHash) {
        HashValue hash = new HashValue(HashingAlgorithm.SHA256, itemHash);
        return register.getBlob(hash);
    }
}
