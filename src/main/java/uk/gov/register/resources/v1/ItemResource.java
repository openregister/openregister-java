package uk.gov.register.resources.v1;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.Field;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Optional;

@Path("/items")
public class ItemResource {
    protected final RegisterReadOnly register;
    protected final ViewFactory viewFactory;
    protected final ItemConverter itemConverter;

    @Inject
    public ItemResource(RegisterReadOnly register, ViewFactory viewFactory, ItemConverter itemConverter) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.itemConverter = itemConverter;
    }

    @GET
    @Path("/sha-256:{item-hash}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public AttributionView<ItemView> getBlobWebViewByHex(@PathParam("item-hash") String itemHash) throws FieldConversionException {
        return getItem(itemHash).map(blob -> viewFactory.getAttributionView("item.html", buildItemView(blob)))
                .orElseThrow(() -> new NotFoundException("No item found with item hash: " + itemHash));
    }

    @GET
    @Path("/sha-256:{item-hash}")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_CSV,
    })
    @Timed
    public ItemView getItemDataByHex(@PathParam("item-hash") String blobHash) throws FieldConversionException {
        return getItem(blobHash).map(blob -> buildItemView(blob))
                .orElseThrow(() -> new NotFoundException("No blob found with blob hash: " + blobHash));
    }

    protected Optional<Item> getItem(String itemHash) {
        HashValue hash = new HashValue(HashingAlgorithm.SHA256, itemHash);
        return register.getItemByV1Hash(hash);
    }

    protected Map<String, Field> getFieldsByName() {
        return register.getFieldsByName();
    }

    protected ItemView buildItemView(Item item) {
        return new ItemView(item, register.getFieldsByName());
    }
}
