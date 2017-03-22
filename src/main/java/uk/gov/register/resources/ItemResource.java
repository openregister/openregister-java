package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.*;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Optional;

@Path("/item")
public class ItemResource {
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;

    @Inject
    public ItemResource(RegisterReadOnly register, ViewFactory viewFactory, ItemConverter itemConverter, RegisterMetadata registerMetadata) {
        this.register = register;
        this.viewFactory = viewFactory;
    }

    @GET
    @Path("/sha-256:{item-hash}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public AttributionView<ItemView> getItemWebViewByHex(@PathParam("item-hash") String itemHash) {
        return getItem(itemHash).map(viewFactory::getItemView)
                .orElseThrow(() -> new NotFoundException("No item found with item hash: " + itemHash));
    }

    @GET
    @Path("/sha-256:{item-hash}")
    @Produces({MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_TTL, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV})
    @Timed
    public ItemView getItemDataByHex(@PathParam("item-hash") String itemHash) {
        return getItem(itemHash).map(viewFactory::getItemMediaView)
                .orElseThrow(() -> new NotFoundException("No item found with item hash: " + itemHash));
    }

    private Optional<Item> getItem(String itemHash) {
        HashValue hash = new HashValue(HashingAlgorithm.SHA256, itemHash);
        return register.getItemBySha256(hash);
    }


}
