package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.IntParam;
import uk.gov.register.configuration.IndexConfiguration;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterName;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.providers.params.IntegerParam;
import uk.gov.register.views.*;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Path("/")
public class DerivationRecordResource {
    private final HttpServletResponseAdapter httpServletResponseAdapter;
    private final RequestContext requestContext;
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;
    private final RegisterName registerPrimaryKey;
    private final Provider<IndexConfiguration> indexConfiguration;

    @Inject
    public DerivationRecordResource(RegisterReadOnly register, ViewFactory viewFactory, RequestContext requestContext,
                                    Provider<IndexConfiguration> indexConfiguration) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.httpServletResponse);
        this.registerPrimaryKey = register.getRegisterName();
        this.indexConfiguration = indexConfiguration;
    }

    @GET
    @Path("/index/{index-name}/record/{record-key}")
    @Produces({MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public RecordView getRecordByKey(@PathParam("index-name") String indexName, @PathParam("record-key") String key) {
        ensureIndexIsAccessible(indexName);

        return register.getRecord(key, indexName)
                .map(r -> viewFactory.getRecordMediaView(r))
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/index/{index-name}/record/{record-key}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public AttributionView<RecordView> getRecordByKeyHtml(@PathParam("record-key") String key, @PathParam("index-name") String indexName) {
        ensureIndexIsAccessible(indexName);

        return viewFactory.getRecordView(getRecordByKey(indexName, key));
    }

    @GET
    @Path("/index/{index-name}/records")
    @Produces({MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    @Timed
    public RecordsView records(@PathParam("index-name") String indexName, @QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) {
        ensureIndexIsAccessible(indexName);

        IndexSizePagination pagination = setUpPagination(pageIndex, pageSize, indexName);
        setContentDisposition();
        return getRecordsView(pagination.pageSize(), pagination.offset(), indexName);
    }

    @GET
    @Path("/index/{index-name}/records")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public PaginatedView<RecordsView> recordsHtml(@PathParam("index-name") String indexName, @QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) {
        ensureIndexIsAccessible(indexName);

        IndexSizePagination pagination = setUpPagination(pageIndex, pageSize, indexName);
        setContentDisposition();
        RecordsView recordsView = getRecordsView(pagination.pageSize(), pagination.offset(), indexName);
        return viewFactory.getRecordsView(pagination, recordsView);
    }

    protected void ensureIndexIsAccessible(String indexName) {
        if (!indexConfiguration.get().getIndexes().contains(indexName)) {
            throw new NotFoundException();
        }
    }

    private IndexSizePagination setUpPagination(@QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize, String indexName) {
        IndexSizePagination pagination = new IndexSizePagination(pageIndex.map(IntParam::get), pageSize.map(IntParam::get), register.getTotalRecords(indexName));

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

    private RecordsView getRecordsView(int limit, int offset, String indexName) {
        List<Record> records = register.getRecords(limit, offset, indexName);
        return viewFactory.getIndexRecordsMediaView(records);
    }
}
