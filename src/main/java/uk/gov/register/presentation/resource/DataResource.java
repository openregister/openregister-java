package uk.gov.register.presentation.resource;

import io.dropwizard.views.View;
import uk.gov.register.presentation.ArchiveCreator;
import uk.gov.register.presentation.RegisterDetail;
import uk.gov.register.presentation.dao.*;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/")
public class DataResource {
    protected final ViewFactory viewFactory;
    private final ItemDAO itemDAO;
    private RequestContext requestContext;
    private RecordDAO recordDAO;
    private final EntryDAO entryDAO;

    @Inject
    public DataResource(ViewFactory viewFactory, RequestContext requestContext, RecordDAO recordDAO, EntryDAO entryDAO, ItemDAO itemDAO) {
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.recordDAO = recordDAO;
        this.entryDAO = entryDAO;
        this.itemDAO = itemDAO;
    }

    @GET
    @Path("/download-register")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, ExtraMediaType.TEXT_HTML})
    @DownloadNotAvailable
    public Response downloadRegister() {
        Collection<Entry> entries = entryDAO.getAllEntriesNoPagination();
        Collection<Item> items = itemDAO.getAllItemsNoPagination();

        int totalEntries = entryDAO.getTotalEntries();
        int totalRecords = recordDAO.getTotalRecords();

        RegisterDetail registerDetail = viewFactory.registerDetailView(
                totalRecords,
                totalEntries,
                totalEntries,
                entryDAO.getLastUpdatedTime()
        ).getRegisterDetail();

        return Response
                .ok(new ArchiveCreator().create(registerDetail, entries, items))
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
}

