package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.IntParam;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterName;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.providers.params.IntegerParam;
import uk.gov.register.views.*;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Path("/")
public class DerivationRecordResource {
    private final HttpServletResponseAdapter httpServletResponseAdapter;
    private final RequestContext requestContext;
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;
    private final RegisterName registerPrimaryKey;

    @Inject
    public DerivationRecordResource(RegisterReadOnly register, ViewFactory viewFactory, RequestContext requestContext) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.httpServletResponse);
        this.registerPrimaryKey = register.getRegisterName();
    }

    @GET
    @Path("/index/{index-name}/record/{record-key}")
    @Produces({MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public RecordView getRecordByKey(@PathParam("index-name") String indexName, @PathParam("record-key") String key) {
        return register.getDerivationRecord(key, indexName).map(viewFactory::getRecordMediaView)
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/index/{index-name}/record/{record-key}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public AttributionView<RecordView> getRecordByKeyHtml(@PathParam("record-key") String key, @PathParam("index-name") String indexName) {
        return viewFactory.getRecordView(getRecordByKey(key, indexName));
    }

    @GET
    @Path("/index/{index-name}/records")
    @Produces({MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    @Timed
    public RecordsView records(@PathParam("index-name") String indexName, @QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) {
        IndexSizePagination pagination = setUpPagination(pageIndex, pageSize);
        setContentDisposition();
        return getRecordsView(pagination.pageSize(), pagination.offset(), indexName);
    }

    @GET
    @Path("/index/{index-name}/records")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public PaginatedView<RecordsView> recordsHtml(@PathParam("index-name") String indexName, @QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) {
        IndexSizePagination pagination = setUpPagination(pageIndex, pageSize);
        setContentDisposition();
        RecordsView recordsView = getRecordsView(pagination.pageSize(), pagination.offset(), indexName);
        return viewFactory.getRecordListView(pagination, recordsView);
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

    private RecordsView getRecordsView(int limit, int offset, String indexName) {
        List<Record> records = register.getDerivationRecords(limit, offset, indexName);
        List<RecordView> recordViews = records.stream().map(viewFactory::getRecordMediaView).collect(toList());
        return viewFactory.getRecordsMediaView(recordViews);
    }


}
