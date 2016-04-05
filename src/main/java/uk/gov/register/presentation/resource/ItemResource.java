package uk.gov.register.presentation.resource;

import io.dropwizard.jersey.caching.CacheControl;
import org.postgresql.util.PSQLException;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.dao.Item;
import uk.gov.register.presentation.dao.ItemDAO;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.AttributionView;
import uk.gov.register.presentation.view.ItemView;
import uk.gov.register.presentation.view.SingleEntryView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.function.Function;

@Path("/item")
public class ItemResource {
    private final ViewFactory viewFactory;
    private final ItemDAO itemDAO;
    private final RecentEntryIndexQueryDAO queryDAO;
    private final RequestContext requestContext;
    private static final String POSTGRES_TABLE_NOT_EXIST_ERROR_CODE = "42P01";

    @Inject
    public ItemResource(ViewFactory viewFactory, RequestContext requestContext, ItemDAO itemDAO, RecentEntryIndexQueryDAO queryDAO) {
        this.viewFactory = viewFactory;
        this.itemDAO = itemDAO;
        this.queryDAO = queryDAO;
        this.requestContext = requestContext;
    }

    @GET
    @Path("/{item-hash}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @CacheControl(immutable = true)
    public AttributionView getItemByHex(@PathParam("item-hash") String itemHash) {
        String sha256Regex = "(sha-256:)(.*)";
        if(itemHash.matches(sha256Regex)){
            return getItemBySHA256(itemHash.replaceAll(sha256Regex, "$2"));
        }else{
            return getItemByHash(itemHash);
        }

    }

    @Deprecated
    private SingleEntryView getItemByHash(String itemHash) {
        Optional<DbEntry> entryO = queryDAO.findEntryByHash(itemHash);
        return entryResponse(entryO, viewFactory::getSingleEntryView);
    }

    private ItemView getItemBySHA256(String sha256Hash) {
        Optional<Item> item;
        try {
            item = itemDAO.getItemBySHA256(sha256Hash);
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            if (cause instanceof PSQLException && ((PSQLException) cause).getSQLState().equals(POSTGRES_TABLE_NOT_EXIST_ERROR_CODE)) {
                throw new NotFoundException();
            }
            throw e;
        }
        return item.map(viewFactory::getItemView).orElseThrow(NotFoundException::new);
    }

    private SingleEntryView entryResponse(Optional<DbEntry> optionalEntry, Function<DbEntry, SingleEntryView> convertToEntryView) {
        SingleEntryView singleEntryView = optionalEntry.map(convertToEntryView)
                .orElseThrow(NotFoundException::new);

        requestContext.
                getHttpServletResponse().
                setHeader("Link", String.format("<%s>;rel=\"version-history\"", singleEntryView.getVersionHistoryLink()));

        return singleEntryView;
    }
}
