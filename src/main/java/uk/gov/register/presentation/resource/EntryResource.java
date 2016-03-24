package uk.gov.register.presentation.resource;

import io.dropwizard.jersey.caching.CacheControl;
import org.postgresql.util.PSQLException;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.EntryDAO;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.AttributionView;
import uk.gov.register.presentation.view.SingleEntryView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.function.Function;

@Path("/entry")
public class EntryResource {

    private final EntryDAO entryDAO;
    private final ViewFactory viewFactory;
    private final RecentEntryIndexQueryDAO queryDAO;
    private final RequestContext requestContext;


    @Inject
    public EntryResource(EntryDAO entryDAO, ViewFactory viewFactory, RecentEntryIndexQueryDAO queryDAO, RequestContext requestContext) {
        this.entryDAO = entryDAO;
        this.viewFactory = viewFactory;
        this.queryDAO = queryDAO;
        this.requestContext = requestContext;
    }

    @GET
    @Path("/{entry-number}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @CacheControl(immutable = true)
    public AttributionView findByEntryNumber(@PathParam("entry-number") int entryNumber) {
        try {
            Optional<Entry> entry = entryDAO.findByEntryNumber(entryNumber);
            return entry.map(viewFactory::getNewEntryView).orElseThrow(NotFoundException::new);
        } catch (Throwable e) {
            if (e.getCause() instanceof PSQLException && ((PSQLException) e.getCause()).getSQLState().equals("42P01")) {
                return findBySerial(Optional.of(entryNumber));
            }
            throw e;
        }
    }

    private SingleEntryView findBySerial(Optional<Integer> serial) {
        Optional<DbEntry> entryO = serial.flatMap(queryDAO::findEntryBySerialNumber);
        return entryResponse(entryO, viewFactory::getSingleEntryView);
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
