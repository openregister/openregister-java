package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.IntParam;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterMetadata;
import uk.gov.register.core.RegisterName;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.providers.params.IntegerParam;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.RecordView;
import uk.gov.register.views.RecordsView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Path("/index/{index-name}")
public class DerivationRecordResource {
    private final HttpServletResponseAdapter httpServletResponseAdapter;
    private final RequestContext requestContext;
    private final RegisterReadOnly register;
    private final ViewFactory viewFactory;
    private final RegisterName registerPrimaryKey;
    private final ItemConverter itemConverter;

    @Inject
    public DerivationRecordResource(RegisterReadOnly register, ViewFactory viewFactory, RequestContext requestContext, ItemConverter itemConverter, RegisterMetadata registerMetadata) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.httpServletResponse);
        this.registerPrimaryKey = register.getRegisterName();
        this.itemConverter = itemConverter;
    }


    @GET
    @Path("/record/{record-key}")
    @Produces({MediaType.APPLICATION_JSON})
    public RecordView getRecordByKey(@PathParam("index-name") String indexName, @PathParam("record-key") String key) throws IOException {
        return register.getDerivationRecord(key, indexName).map(viewFactory::getRecordMediaView)
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/records")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public RecordsView records(@PathParam("index-name") String indexName, @QueryParam(IndexSizePagination.INDEX_PARAM) Optional<IntegerParam> pageIndex, @QueryParam(IndexSizePagination.SIZE_PARAM) Optional<IntegerParam> pageSize) {
        IndexSizePagination pagination = setUpPagination(pageIndex, pageSize);
        setContentDisposition();
        return getRecordsView(pagination.pageSize(), pagination.offset(), indexName);
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
