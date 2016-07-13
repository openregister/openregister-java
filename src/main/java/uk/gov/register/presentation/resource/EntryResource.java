package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.EntryQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.AttributionView;
import uk.gov.register.presentation.view.EntryListView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Optional;

@Path("/")
public class EntryResource {

    private final EntryQueryDAO entryDAO;
    private final ViewFactory viewFactory;
    private final RequestContext requestContext;
    private final HttpServletResponseAdapter httpServletResponseAdapter;

    @Inject
    public EntryResource(EntryQueryDAO entryDAO, ViewFactory viewFactory, RequestContext requestContext) {
        this.entryDAO = entryDAO;
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.httpServletResponse);
    }

    @GET
    @Path("/entry/{entry-number}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public AttributionView findByEntryNumber(@PathParam("entry-number") int entryNumber) {
        Optional<Entry> entry = entryDAO.findByEntryNumber(entryNumber);
        return entry.map(viewFactory::getEntryView).orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/entries")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public EntryListView entries(@QueryParam("start") Optional<Integer> optionalStart, @QueryParam("limit") Optional<Integer> optionalLimit) {
        int totalEntries = entryDAO.getTotalEntries();
        NewPagination newPagination = new NewPagination(optionalStart, optionalLimit, totalEntries);

        Collection<Entry> entries = entryDAO.getEntries(newPagination.start, newPagination.limit);

        setHeaders(newPagination);

        return viewFactory.getEntriesView(entries, newPagination);
    }

    private void setHeaders(NewPagination newPagination) {
        requestContext.resourceExtension().ifPresent(
                ext -> httpServletResponseAdapter.addContentDispositionHeader(requestContext.getRegisterPrimaryKey() + "-entries." + ext)
        );

        if (newPagination.hasNextPage()) {
            httpServletResponseAdapter.addLinkHeader("next", newPagination.getNextPageLink());
        }

        if (newPagination.hasPreviousPage()) {
            httpServletResponseAdapter.addLinkHeader("previous", newPagination.getPreviousPageLink());
        }
    }
}

