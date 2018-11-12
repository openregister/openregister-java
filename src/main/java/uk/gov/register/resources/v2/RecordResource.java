package uk.gov.register.resources.v2;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.IntParam;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.providers.params.IntegerParam;
import uk.gov.register.resources.HttpServletResponseAdapter;
import uk.gov.register.resources.IndexSizePagination;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;
import uk.gov.register.views.v2.EntryListView;
import uk.gov.register.views.v2.RecordListView;
import uk.gov.register.views.v2.RecordView;

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
import java.util.Map;
import java.util.Optional;


@Path("/next/")
public class RecordResource {
    protected final HttpServletResponseAdapter httpServletResponseAdapter;
    protected final RegisterReadOnly register;
    protected final ViewFactory viewFactory;
    protected final ItemConverter itemConverter;

    @Inject
    public RecordResource(RegisterReadOnly register, ViewFactory viewFactory, RequestContext requestContext, ItemConverter itemConverter) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.itemConverter = itemConverter;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.getHttpServletResponse());
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

        return register.getRecord(EntryType.user, key).map(this::buildRecordView)
                .orElseThrow(NotFoundException::new);
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
        return new EntryListView(allEntries);
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
    public RecordListView facetedRecords(@PathParam("key") String key, @PathParam("value") String value) throws FieldConversionException {
        List<Record> records = register.max100RecordsFacetedByKeyValue(key, value);
        return buildRecordListView(records);
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
    public RecordListView records(@QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) throws FieldConversionException {
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

    private RecordListView getRecordsView(int limit, int offset) throws FieldConversionException {
        List<Record> records = register.getRecords(EntryType.user, limit, offset);
        return buildRecordListView(records);
    }

    private RecordListView buildRecordListView(Collection<Record> records) {
        return new RecordListView(records, register.getFieldsByName());
    }

    private RecordView buildRecordView(final Record record) throws FieldConversionException {
        Map<String, FieldValue> itemKeyValuePairs = itemConverter.convertItem(record.getItem(), register.getFieldsByName());
        return new RecordView(record, itemKeyValuePairs, register.getFieldsByName().values());
    }
}
