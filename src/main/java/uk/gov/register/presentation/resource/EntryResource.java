package uk.gov.register.presentation.resource;

import io.dropwizard.jersey.caching.CacheControl;
import org.postgresql.util.PSQLException;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.EntryDAO;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.function.Function;

@Path("/")
public class EntryResource extends ResourceCommon {

    private final EntryDAO entryDAO;
    private final ViewFactory viewFactory;
    private final RecentEntryIndexQueryDAO queryDAO;
    private static final String POSTGRES_TABLE_NOT_EXIST_ERROR_CODE = "42P01";


    @Inject
    public EntryResource(EntryDAO entryDAO, ViewFactory viewFactory, RecentEntryIndexQueryDAO queryDAO, RequestContext requestContext) {
        super(requestContext);
        this.entryDAO = entryDAO;
        this.viewFactory = viewFactory;
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("/entry/{entry-number}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @CacheControl(immutable = true)
    public AttributionView findByEntryNumber(@PathParam("entry-number") int entryNumber) {
        try {
            Optional<Entry> entry = entryDAO.findByEntryNumber(entryNumber);
            return entry.map(viewFactory::getNewEntryView).orElseThrow(NotFoundException::new);
        } catch (Throwable e) {
            //Todo: this is required to support the old resource response till the migration is not completed
            if (e.getCause() instanceof PSQLException && ((PSQLException) e.getCause()).getSQLState().equals(POSTGRES_TABLE_NOT_EXIST_ERROR_CODE)) {
                return findBySerial(Optional.of(entryNumber));
            }
            throw e;
        }
    }

    @GET
    @Path("/entries")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public AttributionView entries(@QueryParam(Pagination.INDEX_PARAM) Optional<Long> pageIndex, @QueryParam(Pagination.SIZE_PARAM) Optional<Long> pageSize) {
        Pagination pagination = new Pagination(pageIndex, pageSize, queryDAO.getTotalEntries());

        setNextAndPreviousPageLinkHeader(pagination);

        getFileExtension().ifPresent(ext -> addContentDispositionHeader(requestContext.getRegisterPrimaryKey() + "-entries." + ext));
        return viewFactory.getNewEntriesView(entryDAO.getEntries(pagination.pageSize(), pagination.offset()), pagination);
    }

    @Deprecated
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
