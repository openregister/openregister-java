package uk.gov.register.resources;

import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/item")
public class ItemResource {
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;

    @Inject
    public ItemResource(RegisterReadOnly register, ViewFactory viewFactory) {
        this.register = register;
        this.viewFactory = viewFactory;
    }

    @GET
    @Path("/{item-hash}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public AttributionView getItemByHex(@PathParam("item-hash") String itemHash) {
        try {
            HashValue hash = HashValue.decode(HashingAlgorithm.SHA256, itemHash);
            return getItemBySHA256(hash);
        } catch (Exception e) {
            throw new NotFoundException("No item found with item hash: " + itemHash);
        }
    }

    private ItemView getItemBySHA256(HashValue hash) {
        return register.getItemBySha256(hash).map(viewFactory::getItemView).orElseThrow(NotFoundException::new);
    }
}