package uk.gov.register.resources;

import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/item")
public class ItemResource {
    private final ViewFactory viewFactory;
    private final ItemQueryDAO itemDAO;

    @Inject
    public ItemResource(ViewFactory viewFactory, ItemQueryDAO itemDAO) {
        this.viewFactory = viewFactory;
        this.itemDAO = itemDAO;
    }

    @GET
    @Path("/{item-hash}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public AttributionView getItemByHex(@PathParam("item-hash") String itemHash) {
        String sha256Regex = "(sha-256:)(.*)";
        if (itemHash.matches(sha256Regex)) {
            return getItemBySHA256(itemHash.replaceAll(sha256Regex, "$2"));
        }
        throw new NotFoundException("No item found with item hex: " + sha256Regex);
    }

    private ItemView getItemBySHA256(String sha256Hash) {
        return itemDAO.getItemBySHA256(sha256Hash).map(viewFactory::getItemView).orElseThrow(NotFoundException::new);
    }
}
