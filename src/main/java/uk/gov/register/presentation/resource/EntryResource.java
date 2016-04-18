package uk.gov.register.presentation.resource;

import io.dropwizard.jersey.caching.CacheControl;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.EntryDAO;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.AttributionView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("/")
public class EntryResource extends ResourceCommon {

    private final EntryDAO entryDAO;
    private final ViewFactory viewFactory;
    private final RecentEntryIndexQueryDAO queryDAO;

    @Inject
    public EntryResource(EntryDAO entryDAO, ViewFactory viewFactory, RecentEntryIndexQueryDAO queryDAO, RequestContext requestContext) {
        super(requestContext);
        this.entryDAO = entryDAO;
        this.viewFactory = viewFactory;
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("/entry/{entry-number}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV})
    @CacheControl(immutable = true)
    public AttributionView findByEntryNumber(@PathParam("entry-number") int entryNumber) {
        Optional<Entry> entry = entryDAO.findByEntryNumber(entryNumber);
        return entry.map(viewFactory::getNewEntryView).orElseThrow(NotFoundException::new);
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
}
