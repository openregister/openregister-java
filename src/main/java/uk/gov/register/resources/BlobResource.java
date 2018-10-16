package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Blob;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.BlobView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("/v2/blobs")
public class BlobResource {
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;

    @Inject
    public BlobResource(RegisterReadOnly register, ViewFactory viewFactory) {
        this.register = register;
        this.viewFactory = viewFactory;
    }

    @GET
    @Path("/sha-256:{blob-hash}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public AttributionView<BlobView> getBlobWebViewByHex(@PathParam("blob-hash") String blobHash) throws FieldConversionException {
        return getBlob(blobHash).map(viewFactory::getBlobView)
                .orElseThrow(() -> new NotFoundException("No item found with hash: " + blobHash));
    }

    @GET
    @Path("/sha-256:{blob-hash}")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_YAML,
            ExtraMediaType.TEXT_TTL,
            ExtraMediaType.TEXT_CSV,
            ExtraMediaType.TEXT_TSV,
            ExtraMediaType.APPLICATION_SPREADSHEET
    })
    @Timed
    public BlobView getBlobDataByHex(@PathParam("blob-hash") String blobHash) throws FieldConversionException {
        return getBlob(blobHash).map(viewFactory::getBlobMediaView)
                .orElseThrow(() -> new NotFoundException("No blob found with hash: " + blobHash));
    }

    private Optional<Blob> getBlob(String blobHash) {
        HashValue hash = new HashValue(HashingAlgorithm.SHA256, blobHash);
        return register.getBlob(hash);
    }
}
