package uk.gov.register.resources.v1;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.resources.v2.BlobResource;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.ItemListView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.stream.Collectors;

@Path("/items")
public class ItemResource extends BlobResource {
    @Inject
    public ItemResource(RegisterReadOnly register, ViewFactory viewFactory, ItemConverter itemConverter) {
        super(register, viewFactory, itemConverter);
    }

    @GET
    @Path("/sha-256:{item-hash}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public AttributionView<ItemView> getBlobWebViewByHex(@PathParam("item-hash") String itemHash) throws FieldConversionException {
        return getBlob(itemHash).map(blob -> viewFactory.getAttributionView("item.html", buildItemView(blob)))
                .orElseThrow(() -> new NotFoundException("No item found with item hash: " + itemHash));
    }

    @GET
    @Path("/")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public AttributionView<ItemListView> listBlobsWebView() {
        Collection<Item> items = register.getAllItems(EntryType.user);
        ItemListView itemListView = buildItemListView(items.stream().limit(100).collect(Collectors.toList()));

        // TODO: allow this resource to be paginated
        return viewFactory.getAttributionView("items.html", itemListView);
    }
}
