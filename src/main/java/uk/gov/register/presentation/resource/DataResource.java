package uk.gov.register.presentation.resource;

import io.dropwizard.views.View;
import uk.gov.register.presentation.ArchiveCreator;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.RegisterDetail;
import uk.gov.register.presentation.dao.EntryDAO;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.dao.RecordDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.EntryListView;
import uk.gov.register.presentation.view.RegisterDetailView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/")
public class DataResource extends ResourceCommon {
    protected final ViewFactory viewFactory;
    private final RecentEntryIndexQueryDAO queryDAO;
    private RecordDAO recordDAO;
    private final EntryDAO entryDAO;

    @Inject
    public DataResource(ViewFactory viewFactory, RequestContext requestContext, RecentEntryIndexQueryDAO queryDAO, RecordDAO recordDAO, EntryDAO entryDAO) {
        super(requestContext);
        this.viewFactory = viewFactory;
        this.queryDAO = queryDAO;
        this.recordDAO = recordDAO;
        this.entryDAO = entryDAO;
    }

    @GET
    @Path("/download-register")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, ExtraMediaType.TEXT_HTML})
    @DownloadNotAvailable
    public Response downloadRegister() {
        List<DbEntry> entries = queryDAO.getAllEntriesNoPagination();

        int totalEntries = entryDAO.getTotalEntries();
        int totalRecords = recordDAO.getTotalRecords();

        RegisterDetail registerDetail = viewFactory.registerDetailView(
                totalRecords,
                totalEntries,
                totalEntries,
                entryDAO.getLastUpdatedTime()
        ).getRegisterDetail();

        return Response
                .ok(new ArchiveCreator().create(registerDetail, entries))
                .header("Content-Disposition", String.format("attachment; filename=%s-%d.zip", requestContext.getRegisterPrimaryKey(), System.currentTimeMillis()))
                .header("Content-Transfer-Encoding", "binary")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                .build();
    }

    @GET
    @Path("/download")
    @Produces(ExtraMediaType.TEXT_HTML)
    @DownloadNotAvailable
    public View download() {
        return viewFactory.thymeleafView("download.html");
    }

    @GET
    @Path("/download.torrent")
    @Produces(ExtraMediaType.TEXT_HTML)
    @DownloadNotAvailable
    public Response downloadTorrent() {
        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .entity(viewFactory.thymeleafView("not-implemented.html"))
                .build();
    }

    @GET
    @Path("/records")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public EntryListView records(@QueryParam(Pagination.INDEX_PARAM) Optional<Long> pageIndex, @QueryParam(Pagination.SIZE_PARAM) Optional<Long> pageSize) {
        Pagination pagination = new Pagination(pageIndex, pageSize, recordDAO.getTotalRecords());

        setNextAndPreviousPageLinkHeader(pagination);

        getFileExtension().ifPresent(ext -> addContentDispositionHeader(requestContext.getRegisterPrimaryKey() + "-records." + ext));
        return viewFactory.getRecordsView(queryDAO.getLatestEntriesOfRecords(pagination.pageSize(), pagination.offset()), pagination);
    }

    @GET
    @Path("/register")
    @Produces({MediaType.APPLICATION_JSON})
    public RegisterDetailView getRegisterDetail() {
        return viewFactory.registerDetailView(recordDAO.getTotalRecords(), entryDAO.getTotalEntries(), entryDAO.getTotalEntries(), entryDAO.getLastUpdatedTime());
    }
}

