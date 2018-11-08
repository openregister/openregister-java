package uk.gov.register.resources.v1;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.IntParam;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.exceptions.NoSuchFieldException;
import uk.gov.register.providers.params.IntegerParam;
import uk.gov.register.resources.HttpServletResponseAdapter;
import uk.gov.register.resources.IndexSizePagination;
import uk.gov.register.resources.Pagination;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.EntryListView;
import uk.gov.register.views.PaginatedView;
import uk.gov.register.views.RecordView;
import uk.gov.register.views.RecordsView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Path("/")
public class RecordResource {
    protected final HttpServletResponseAdapter httpServletResponseAdapter;
    protected final RegisterReadOnly register;
    protected final ViewFactory viewFactory;

    @Inject
    public RecordResource(RegisterReadOnly register, ViewFactory viewFactory, RequestContext requestContext) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.getHttpServletResponse());
    }


    @GET
    @Path("/records/{record-key}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public AttributionView<RecordView> getRecordByKeyHtml(@PathParam("record-key") String key) throws FieldConversionException {
        return viewFactory.getRecordView(getRecordByKey(key));
    }

    @GET
    @Path("/records/{record-key}")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_YAML,
            ExtraMediaType.TEXT_CSV,
            ExtraMediaType.TEXT_TSV,
            ExtraMediaType.TEXT_TTL,
            ExtraMediaType.APPLICATION_SPREADSHEET
    })
    @Timed
    public RecordView getRecordByKey(@PathParam("record-key") String key) throws FieldConversionException {
        httpServletResponseAdapter.setLinkHeader("version-history", String.format("/records/%s/entries", key));

        return register.getRecord(EntryType.user, key).map(viewFactory::getRecordMediaView)
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/records/{record-key}/entries")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public PaginatedView<EntryListView> getAllEntriesOfARecordHtml(@PathParam("record-key") String key) {
        Collection<Entry> allEntries = register.allEntriesOfRecord(key);
        if (allEntries.isEmpty()) {
            throw new NotFoundException();
        }

        EntryListView entryListView = new EntryListView(allEntries, key);
        Pagination pagination = new IndexSizePagination(Optional.of(1), Optional.of(allEntries.size()), allEntries.size());

        return viewFactory.getPaginatedView("entries.html", entryListView, pagination);
    }

    @GET
    @Path("/records/{record-key}/entries")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_YAML,
            ExtraMediaType.TEXT_CSV,
            ExtraMediaType.TEXT_TSV,
            ExtraMediaType.TEXT_TTL,
            ExtraMediaType.APPLICATION_SPREADSHEET
    })
    @Timed
    public EntryListView getAllEntriesOfARecord(@PathParam("record-key") String key) {
        Collection<Entry> allEntries = register.allEntriesOfRecord(key);
        if (allEntries.isEmpty()) {
            throw new NotFoundException();
        }
        return new EntryListView(allEntries, key);
    }

    @GET
    @Path("/records/{key}/{value}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public PaginatedView<RecordsView> facetedRecordsHtml(@PathParam("key") String key, @PathParam("value") String value) throws FieldConversionException, NoSuchFieldException {
        List<Record> records = register.max100RecordsFacetedByKeyValue(key, value);
        Pagination pagination
                = new IndexSizePagination(Optional.empty(), Optional.empty(), records.size());
        return viewFactory.getRecordsView(pagination, facetedRecords(key, value));
    }

    @GET
    @Path("/records/{key}/{value}")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_YAML,
            ExtraMediaType.TEXT_CSV,
            ExtraMediaType.TEXT_TSV,
            ExtraMediaType.TEXT_TTL,
            ExtraMediaType.APPLICATION_SPREADSHEET
    })
    @Timed
    public RecordsView facetedRecords(@PathParam("key") String key, @PathParam("value") String value) throws FieldConversionException {
        List<Record> records = register.max100RecordsFacetedByKeyValue(key, value);
        return viewFactory.getRecordsMediaView(records);
    }

    @GET
    @Path("/records")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public PaginatedView<RecordsView> recordsHtml(@QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) throws FieldConversionException {
        IndexSizePagination pagination = setUpPagination(pageIndex, pageSize);
        RecordsView recordsView = getRecordsView(pagination.pageSize(), pagination.offset());
        return viewFactory.getRecordsView(pagination, recordsView);
    }

    @GET
    @Path("/records")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_YAML,
            ExtraMediaType.TEXT_CSV,
            ExtraMediaType.TEXT_TSV,
            ExtraMediaType.TEXT_TTL,
            ExtraMediaType.APPLICATION_SPREADSHEET
    })
    @Timed
    public RecordsView records(@QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) throws FieldConversionException {
        IndexSizePagination pagination = setUpPagination(pageIndex, pageSize);

        return getRecordsView(pagination.pageSize(), pagination.offset());
    }

    private IndexSizePagination setUpPagination(@QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) {
        IndexSizePagination pagination = new IndexSizePagination(pageIndex.map(IntParam::get), pageSize.map(IntParam::get), register.getTotalRecords(EntryType.user));

        if (pagination.hasNextPage()) {
            httpServletResponseAdapter.setLinkHeader("next", pagination.getNextPageLink());
        }

        if (pagination.hasPreviousPage()) {
            httpServletResponseAdapter.setLinkHeader("previous", pagination.getPreviousPageLink());
        }
        return pagination;
    }

    private RecordsView getRecordsView(int limit, int offset) throws FieldConversionException {
        List<Record> records = register.getRecords(EntryType.user, limit, offset);
        return viewFactory.getRecordsMediaView(records);
    }
}
