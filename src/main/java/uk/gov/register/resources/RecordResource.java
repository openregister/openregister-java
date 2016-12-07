package uk.gov.register.resources;

import io.dropwizard.jersey.params.IntParam;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.FieldValue;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.providers.params.IntegerParam;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.EntryListView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.PaginatedView;
import uk.gov.register.views.RecordsView;
import uk.gov.register.views.RecordView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Path("/")
public class RecordResource {
    private final HttpServletResponseAdapter httpServletResponseAdapter;
    private final RequestContext requestContext;
    private final RegisterData registerData;
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;
    private final String registerPrimaryKey;
    private final ItemConverter itemConverter;

    @Inject
    public RecordResource(RegisterData registerData, RegisterReadOnly register, ViewFactory viewFactory, RequestContext requestContext, RegisterNameConfiguration registerNameConfiguration, ItemConverter itemConverter) {
        this.registerData = registerData;
        this.register = register;
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.httpServletResponse);
        registerPrimaryKey = registerNameConfiguration.getRegisterName();
        this.itemConverter = itemConverter;
    }

    @GET
    @Path("/record/{record-key}")
    @Produces(ExtraMediaType.TEXT_HTML)
    public AttributionView<RecordView> getRecordByKeyHtml(@PathParam("record-key") String key) {
        return viewFactory.getRecordView(getRecordByKey(key));
    }

    @GET
    @Path("/record/{record-key}")
    @Produces({MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public RecordView getRecordByKey(@PathParam("record-key") String key) {
        httpServletResponseAdapter.addLinkHeader("version-history", String.format("/record/%s/entries", key));

        return register.getRecord(key).map(this::toRecordView)
                .orElseThrow(NotFoundException::new);
    }

    public Map<String, FieldValue> itemContent(Record record) {
        return record.item.getFieldsStream().collect(Collectors.toMap(Map.Entry::getKey, itemConverter::convert));
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
    @Produces(ExtraMediaType.TEXT_HTML)
    public PaginatedView<RecordsView> facetedRecordsHtml(@PathParam("key") String key, @PathParam("value") String value) {
        List<Record> records = register.max100RecordsFacetedByKeyValue(key, value);
        Pagination pagination
                = new IndexSizePagination(Optional.empty(), Optional.empty(), records.size());
        return viewFactory.getRecordListView(pagination, facetedRecords(key, value));
    }

    @GET
    @Path("/records/{key}/{value}")
    @Produces({MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public RecordsView facetedRecords(@PathParam("key") String key, @PathParam("value") String value) {
        List<Record> records = register.max100RecordsFacetedByKeyValue(key, value);
        List<RecordView> recordViews = records.stream().map(this::toRecordView).collect(toList());
        return new RecordsView(recordViews, registerData.getRegister().getFields());
    }

    @GET
    @Path("/records")
    @Produces(ExtraMediaType.TEXT_HTML)
    public PaginatedView<RecordsView> recordsHtml(@QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) {
        IndexSizePagination pagination = setUpPagination(pageIndex, pageSize);
        setContentDisposition();
        RecordsView recordsView = getRecordsView(pagination.pageSize(), pagination.offset());
        return viewFactory.getRecordListView(pagination, recordsView);
    }

    @GET
    @Path("/records")
    @Produces({MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public RecordsView records(@QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) {
        IndexSizePagination pagination = setUpPagination(pageIndex, pageSize);
        setContentDisposition();
        return getRecordsView(pagination.pageSize(), pagination.offset());
    }

    private IndexSizePagination setUpPagination(@QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) {
        IndexSizePagination pagination = new IndexSizePagination(pageIndex.map(IntParam::get), pageSize.map(IntParam::get), register.getTotalRecords());

        if (pagination.hasNextPage()) {
            httpServletResponseAdapter.addLinkHeader("next", pagination.getNextPageLink());
        }

        if (pagination.hasPreviousPage()) {
            httpServletResponseAdapter.addLinkHeader("previous", pagination.getPreviousPageLink());
        }
        return pagination;
    }

    private void setContentDisposition() {
        requestContext.resourceExtension().ifPresent(
                ext -> httpServletResponseAdapter.addInlineContentDispositionHeader(registerPrimaryKey + "-records." + ext)
        );
    }

    private RecordsView getRecordsView(int limit, int offset) {
        List<Record> records = register.getRecords(limit, offset);
        List<RecordView> recordViews = records.stream().map(this::toRecordView).collect(toList());
        return new RecordsView(recordViews, registerData.getRegister().getFields());
    }

    private RecordView toRecordView(Record r) {
        Map<String, FieldValue> itemContent = itemContent(r);
        ItemView itemView = new ItemView(r.item.getSha256hex(), itemContent, registerData.getRegister().getFields());
        return new RecordView(r.entry, itemView, registerData.getRegister().getFields());
    }

}
