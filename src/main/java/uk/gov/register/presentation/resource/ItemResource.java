package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.dao.ItemDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.AttributionView;
import uk.gov.register.presentation.view.ItemView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/item")
public class ItemResource {
    private final ViewFactory viewFactory;
    private final ItemDAO itemDAO;

    @Inject
    public ItemResource(ViewFactory viewFactory, ItemDAO itemDAO) {
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
