package uk.gov.register.presentation.resource;

import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.Record;
import uk.gov.register.presentation.dao.RecordQueryDAO;
import uk.gov.register.api.representations.ExtraMediaType;
import uk.gov.register.presentation.view.*;

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
    private final ViewFactory viewFactory;
    private final RecordQueryDAO recordDAO;
    private final String registerPrimaryKey;

    @Inject
    public RecordResource(ViewFactory viewFactory, RequestContext requestContext, RecordQueryDAO recordDAO, RegisterNameConfiguration registerNameConfiguration) {
        this.viewFactory = viewFactory;
        this.recordDAO = recordDAO;
        this.requestContext = requestContext;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.httpServletResponse);
        registerPrimaryKey = registerNameConfiguration.getRegister();
    }

    @GET
    @Path("/record/{record-key}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public RecordView getRecordByKey(@PathParam("record-key") String key) {
        httpServletResponseAdapter.addLinkHeader("version-history", String.format("/record/%s/entries", key));

        return recordDAO
                .findByPrimaryKey(key)
                .map(viewFactory::getRecordView)
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/record/{record-key}/entries")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public EntryListView getAllEntriesOfARecord(@PathParam("record-key") String key) {
        Collection<Entry> allEntries = recordDAO.findAllEntriesOfRecordBy(registerPrimaryKey, key);
        if (allEntries.isEmpty()) {
            throw new NotFoundException();
        }
        return viewFactory.getRecordEntriesView(
                key, allEntries,
                new Pagination(Optional.of(1), Optional.of(allEntries.size()), allEntries.size())
        );
    }

    @GET
    @Path("/records/{key}/{value}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public RecordListView facetedRecords(@PathParam("key") String key, @PathParam("value") String value) {
        List<Record> records = recordDAO.findMax100RecordsByKeyValue(key, value);
        Pagination pagination
                = new Pagination(Optional.empty(), Optional.empty(), records.size());
        return viewFactory.getRecordListView(records, pagination);
    }

    @GET
    @Path("/records")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public RecordListView records(@QueryParam(Pagination.INDEX_PARAM) Optional<Integer> pageIndex, @QueryParam(Pagination.SIZE_PARAM) Optional<Integer> pageSize) {
        Pagination pagination = new Pagination(pageIndex, pageSize, recordDAO.getTotalRecords());

        requestContext.resourceExtension().ifPresent(
                ext -> httpServletResponseAdapter.addContentDispositionHeader(registerPrimaryKey + "-records." + ext)
        );

        if (pagination.hasNextPage()) {
            httpServletResponseAdapter.addLinkHeader("next", pagination.getNextPageLink());
        }

        if (pagination.hasPreviousPage()) {
            httpServletResponseAdapter.addLinkHeader("previous", pagination.getPreviousPageLink());
        }

        return viewFactory.getRecordListView(recordDAO.getRecords(pagination.pageSize(), pagination.offset()), pagination);
    }

}
