package uk.gov.register.presentation.resource;

import org.postgresql.util.PSQLException;
import uk.gov.register.presentation.dao.Item;
import uk.gov.register.presentation.dao.ItemDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ItemView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

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
    @Path("/{sha256hash: sha-256:.*}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    public ItemView getItemBySHA256Hash(@PathParam("sha256hash") String sha256Hash) {
        Optional<Item> item;
        try {
            item = itemDAO.getItemBySHA256(sha256Hash.replaceAll("(sha-256:)(.*)", "$2"));
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            if (cause instanceof PSQLException && ((PSQLException) cause).getSQLState().equals("42P01")) {
                throw new NotFoundException();
            }
            throw e;
        }
        return item.map(viewFactory::getItemView).orElseThrow(NotFoundException::new);
    }
}
