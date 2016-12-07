package uk.gov.register.resources;

import io.dropwizard.jersey.params.IntParam;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.providers.params.IntegerParam;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.EntryListView;
import uk.gov.register.views.PaginatedView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Optional;

@Path("/")
public class EntryResource {

    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;
    private final RequestContext requestContext;
    private final HttpServletResponseAdapter httpServletResponseAdapter;
    private final String registerPrimaryKey;

    @Inject
    public EntryResource(RegisterReadOnly register, ViewFactory viewFactory, RequestContext requestContext, RegisterNameConfiguration registerNameConfiguration) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.httpServletResponse);
        this.registerPrimaryKey = registerNameConfiguration.getRegisterName();
    }

    @GET
    @Path("/entry/{entry-number}")
    @Produces(ExtraMediaType.TEXT_HTML)
    public AttributionView<Entry> findByEntryNumberHtml(@PathParam("entry-number") int entryNumber) {
        Optional<Entry> entry = findByEntryNumber(entryNumber);
        return entry.map(viewFactory::getEntryView).orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/entry/{entry-number}")
    @Produces({MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public Optional<Entry> findByEntryNumber(@PathParam("entry-number") int entryNumber) {
        return register.getEntry(entryNumber);
    }

    @GET
    @Path("/entries")
    @Produces(ExtraMediaType.TEXT_HTML)
    public PaginatedView<EntryListView> entriesHtml(@QueryParam("start") Optional<IntegerParam> optionalStart, @QueryParam("limit") Optional<IntegerParam> optionalLimit) {
        int totalEntries = register.getTotalEntries();
        StartLimitPagination startLimitPagination = new StartLimitPagination(optionalStart.map(IntParam::get), optionalLimit.map(IntParam::get), totalEntries);

        Collection<Entry> entries = register.getEntries(startLimitPagination.start, startLimitPagination.limit);

        setHeaders(startLimitPagination);

        return viewFactory.getEntriesView(entries, startLimitPagination);
    }

    @GET
    @Path("/entries")
    @Produces({MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public EntryListView entries(@QueryParam("start") Optional<IntegerParam> optionalStart, @QueryParam("limit") Optional<IntegerParam> optionalLimit) {
        int totalEntries = register.getTotalEntries();
        StartLimitPagination startLimitPagination = new StartLimitPagination(optionalStart.map(IntParam::get), optionalLimit.map(IntParam::get), totalEntries);

        Collection<Entry> entries = register.getEntries(startLimitPagination.start, startLimitPagination.limit);

        setHeaders(startLimitPagination);

        return new EntryListView(entries);
    }

    private void setHeaders(StartLimitPagination startLimitPagination) {
        requestContext.resourceExtension().ifPresent(
                ext -> httpServletResponseAdapter.addInlineContentDispositionHeader(registerPrimaryKey + "-entries." + ext)
        );

        if (startLimitPagination.hasNextPage()) {
            httpServletResponseAdapter.addLinkHeader("next", startLimitPagination.getNextPageLink());
        }

        if (startLimitPagination.hasPreviousPage()) {
            httpServletResponseAdapter.addLinkHeader("previous", startLimitPagination.getPreviousPageLink());
        }
    }
}

