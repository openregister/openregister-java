package uk.gov.register.resources.v2;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.IntParam;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.providers.params.IntegerParam;
import uk.gov.register.resources.FutureAPI;
import uk.gov.register.resources.HttpServletResponseAdapter;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.resources.StartLimitPagination;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.EntryListView;
import uk.gov.register.views.PaginatedView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@FutureAPI
@Path("/v2/")
public class EntryResource {

    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;
    private final RequestContext requestContext;
    private final HttpServletResponseAdapter httpServletResponseAdapter;
    private final RegisterId registerPrimaryKey;

    @Inject
    public EntryResource(RegisterReadOnly register, ViewFactory viewFactory, RequestContext requestContext) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.getHttpServletResponse());
        this.registerPrimaryKey = register.getRegisterId();
    }

    @GET
    @Path("/entries")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public PaginatedView<EntryListView> entriesHtml(@QueryParam("start") Optional<IntegerParam> optionalStart, @QueryParam("limit") Optional<IntegerParam> optionalLimit) {
        int totalEntries = register.getTotalEntries(EntryType.user);
        StartLimitPagination startLimitPagination = new StartLimitPagination(optionalStart.map(IntParam::get), optionalLimit.map(IntParam::get), totalEntries);

        Collection<Entry> entries = register.getEntries(startLimitPagination.start, startLimitPagination.limit);

        setHeaders(startLimitPagination);

        return viewFactory.getEntriesView(entries, startLimitPagination);
    }

    @GET
    @Path("/entries/{entry-number}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public AttributionView<Entry> findByEntryNumberHtml(@PathParam("entry-number") int entryNumber) {
        Optional<Entry> entry = register.getEntry(entryNumber);
        return entry.map(viewFactory::getEntryView).orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/entries/{entry-number}")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_YAML,
            ExtraMediaType.TEXT_CSV,
            ExtraMediaType.TEXT_TSV,
            ExtraMediaType.TEXT_TTL,
            ExtraMediaType.APPLICATION_SPREADSHEET
    })
    @Timed
    public Optional<EntryListView> findByEntryNumber(@PathParam("entry-number") int entryNumber) {
        Optional<Entry> entry = register.getEntry(entryNumber);
        return entry.map(function -> new EntryListView(Collections.singletonList(function)));
    }


    @GET
    @Path("/entries")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_YAML,
            ExtraMediaType.TEXT_CSV,
            ExtraMediaType.TEXT_TSV,
            ExtraMediaType.TEXT_TTL,
            ExtraMediaType.APPLICATION_SPREADSHEET
    })
    @Timed
    public EntryListView entries(@QueryParam("start") Optional<IntegerParam> optionalStart, @QueryParam("limit") Optional<IntegerParam> optionalLimit) {
        int totalEntries = register.getTotalEntries(EntryType.user);
        StartLimitPagination startLimitPagination = new StartLimitPagination(optionalStart.map(IntParam::get), optionalLimit.map(IntParam::get), totalEntries);

        Collection<Entry> entries = register.getEntries(startLimitPagination.start, startLimitPagination.limit);

        setHeaders(startLimitPagination);

        return new EntryListView(entries);
    }

    private void setHeaders(StartLimitPagination startLimitPagination) {
        requestContext.resourceExtension().ifPresent(
                ext -> httpServletResponseAdapter.setInlineContentDispositionHeader(registerPrimaryKey + "-entries." + ext)
        );

        if (startLimitPagination.hasNextPage()) {
            httpServletResponseAdapter.setLinkHeader("next", startLimitPagination.getNextPageLink());
        }

        if (startLimitPagination.hasPreviousPage()) {
            httpServletResponseAdapter.setLinkHeader("previous", startLimitPagination.getPreviousPageLink());
        }
    }
}

