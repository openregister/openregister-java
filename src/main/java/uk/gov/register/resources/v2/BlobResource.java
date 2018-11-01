package uk.gov.register.resources.v2;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.IntParam;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.providers.params.IntegerParam;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.resources.StartLimitPagination;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.ItemListView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.PaginatedView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/dev/blobs")
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
    public AttributionView<ItemView> getBlobWebViewByHex(@PathParam("blob-hash") String blobHash) throws FieldConversionException {
        return getBlob(blobHash).map(viewFactory::getItemView)
                .orElseThrow(() -> new NotFoundException("No item found with item hash: " + blobHash));
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
    public ItemView getBlobDataByHex(@PathParam("blob-hash") String blobHash) throws FieldConversionException {
        return getBlob(blobHash).map(viewFactory::getItemMediaView)
                .orElseThrow(() -> new NotFoundException("No item found with item hash: " + blobHash));
    }

    @GET
    @Path("/")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_CSV,
    })
    @Timed
    public ItemListView listBlobs() throws FieldConversionException {
        Collection<Item> items = register.getAllItems(EntryType.user);

        // TODO: allow this resource to be paginated
        // and improve rendering performance
        return viewFactory.getItemsMediaView(items.stream().limit(100).collect(Collectors.toList()));
    }

    @GET
    @Path("/")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public AttributionView<ItemListView> listBlobsWebView() {
        Collection<Item> items = register.getAllItems(EntryType.user);

        // TODO: allow this resource to be paginated
        return viewFactory.getItemListView(items.stream().limit(100).collect(Collectors.toList()));
    }

    private Optional<Item> getBlob(String blobHash) {
        HashValue hash = new HashValue(HashingAlgorithm.SHA256, blobHash);
        return register.getItem(hash);
    }
}
