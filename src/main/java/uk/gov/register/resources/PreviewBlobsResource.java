package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.views.View;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Blob;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path("/")
public class PreviewBlobsResource {

    protected final ViewFactory viewFactory;
    private final RegisterReadOnly register;

    @Inject
    public PreviewBlobsResource(final ViewFactory viewFactory, final RegisterReadOnly register) {
        this.viewFactory = viewFactory;
        this.register = register;
    }

    @GET
    @Path("/preview-items/{media-type}/sha-256:{blob-hash}")
    public Response previewItems(@PathParam("media-type") final String mediaType, @PathParam("blob-hash") final String blobHash) {
        URI location = UriBuilder.fromMethod(getClass(), "jsonByKeyPreview").build(mediaType, blobHash);
        return Response.status(Response.Status.MOVED_PERMANENTLY).location(location).build();
    }

    @GET
    @Path("/preview-blobs/{media-type}/sha-256:{blob-hash}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public View jsonByKeyPreview(@PathParam("media-type") final String mediaType, @PathParam("blob-hash") final String blobHash) throws FieldConversionException {
        final HashValue hash = new HashValue(HashingAlgorithm.SHA256, blobHash);
        final Blob blob = register.getBlob(hash)
                .orElseThrow(() -> new NotFoundException("No blob found with blob hash: " + blobHash));

        return viewFactory.previewItemPageView(blob, blobHash, ExtraMediaType.transform(mediaType));
    }
}
