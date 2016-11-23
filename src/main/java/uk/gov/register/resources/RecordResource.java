package uk.gov.register.resources;

import io.dropwizard.jersey.params.IntParam;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.EntryListView;
import uk.gov.register.views.RecordListView;
import uk.gov.register.views.RecordView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Path("/")
public class RecordResource {
    private final HttpServletResponseAdapter httpServletResponseAdapter;
    private final RequestContext requestContext;
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;
    private final String registerPrimaryKey;

    @Inject
    public RecordResource(RegisterReadOnly register, ViewFactory viewFactory, RequestContext requestContext, RegisterNameConfiguration registerNameConfiguration) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.httpServletResponse);
        registerPrimaryKey = registerNameConfiguration.getRegisterName();
    }

    @GET
    @Path("/record/{record-key}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public RecordView getRecordByKey(@PathParam("record-key") String key) {
        httpServletResponseAdapter.addLinkHeader("version-history", String.format("/record/%s/entries", key));

        return register
                .getRecord(key)
                .map(viewFactory::getRecordView)
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/record/{record-key}/entries")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public EntryListView getAllEntriesOfARecord(@PathParam("record-key") String key) {
        Collection<Entry> allEntries = register.allEntriesOfRecord(key);
        if (allEntries.isEmpty()) {
            throw new NotFoundException();
        }
        return viewFactory.getRecordEntriesView(
                key, allEntries,
                new IndexSizePagination(Optional.of(1), Optional.of(allEntries.size()), allEntries.size())
        );
    }

    @GET
    @Path("/records/{key}/{value}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public RecordListView facetedRecords(@PathParam("key") String key, @PathParam("value") String value) {
        List<Record> records = register.max100RecordsFacetedByKeyValue(key, value);
        Pagination pagination
                = new IndexSizePagination(Optional.empty(), Optional.empty(), records.size());
        return viewFactory.getRecordListView(records, pagination);
    }

    @GET
    @Path("/records")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public RecordListView records(@QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntParam> pageSize) {
        IndexSizePagination pagination = new IndexSizePagination(pageIndex.map(IntParam::get), pageSize.map(IntParam::get), register.getTotalRecords());

        requestContext.resourceExtension().ifPresent(
                ext -> httpServletResponseAdapter.addContentDispositionHeader(registerPrimaryKey + "-records." + ext)
        );

        if (pagination.hasNextPage()) {
            httpServletResponseAdapter.addLinkHeader("next", pagination.getNextPageLink());
        }

        if (pagination.hasPreviousPage()) {
            httpServletResponseAdapter.addLinkHeader("previous", pagination.getPreviousPageLink());
        }

        return viewFactory.getRecordListView(register.getRecords(pagination.pageSize(), pagination.offset()), pagination);
    }

}
