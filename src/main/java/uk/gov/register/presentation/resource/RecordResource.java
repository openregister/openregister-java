package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.Record;
import uk.gov.register.presentation.dao.RecordDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Path("/")
public class RecordResource extends ResourceCommon{
    private ViewFactory viewFactory;
    private RecordDAO recordDAO;

    @Inject
    public RecordResource(ViewFactory viewFactory, RequestContext requestContext, RecordDAO recordDAO) {
        super(requestContext);
        this.viewFactory = viewFactory;
        this.recordDAO = recordDAO;
    }

    @GET
    @Path("/record/{record-key}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV})
    public RecordView getRecordByKey(@PathParam("record-key") String key) {
        requestContext.
                getHttpServletResponse().
                setHeader("Link", String.format("</record/%s/entries>;rel=\"version-history\"", key));

        return recordDAO
                .findByPrimaryKey(key)
                .map(record -> viewFactory.getRecordView(record))
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/record/{record-key}/entries")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV})
    public NewEntryListView getAllEntriesOfARecord(@PathParam("record-key") String key) {
        List<Entry> allEntries = recordDAO.findAllEntriesOfRecordBy(requestContext.getRegisterPrimaryKey(), key);
        if (allEntries.isEmpty()) {
            throw new NotFoundException();
        }
        return viewFactory.getNewEntriesView(
                allEntries,
                new Pagination(Optional.of(1L), Optional.of((long) allEntries.size()), allEntries.size())
        );
    }

    @GET
    @Path("/records/{key}/{value}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV})
    public RecordListView facetedRecords(@PathParam("key") String key, @PathParam("value") String value) {
        List<Record> records = recordDAO.findMax100RecordsByKeyValue(key, value);
        Pagination pagination
                = new Pagination(Optional.empty(), Optional.empty(), records.size());
        return viewFactory.getNewRecordsView(records, pagination);
    }

    @GET
    @Path("/records")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV})
    public RecordListView records(@QueryParam(Pagination.INDEX_PARAM) Optional<Long> pageIndex, @QueryParam(Pagination.SIZE_PARAM) Optional<Long> pageSize) {
        Pagination pagination = new Pagination(pageIndex, pageSize, recordDAO.getTotalRecords());

        setNextAndPreviousPageLinkHeader(pagination);

        getFileExtension().ifPresent(ext -> addContentDispositionHeader(requestContext.getRegisterPrimaryKey() + "-records." + ext));
        return viewFactory.getNewRecordsView(recordDAO.getRecords(pagination.pageSize(), pagination.offset()), pagination);
    }

}
