package uk.gov.register.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import io.dropwizard.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.mint.ItemValidator;
import uk.gov.mint.ObjectReconstructor;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.EntryStore;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.presentation.ArchiveCreator;
import uk.gov.register.core.RegisterDetail;
import uk.gov.register.views.representations.ExtraMediaType;
import uk.gov.register.views.ViewFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/")
public class DataResource {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final ViewFactory viewFactory;
    private final ItemQueryDAO itemDAO;
    private final RecordQueryDAO recordDAO;
    private EntryQueryDAO entryDAO;
    private String registerPrimaryKey;
    private ObjectReconstructor objectReconstructor;
    private ItemValidator itemValidator;
    private EntryStore entryStore;

    @Inject
    public DataResource(ViewFactory viewFactory, RecordQueryDAO recordDAO, EntryQueryDAO entryDAO, ItemQueryDAO itemDAO, RegisterNameConfiguration registerNameConfiguration, ObjectReconstructor objectReconstructor, ItemValidator itemValidator, EntryStore entryStore) {
        this.viewFactory = viewFactory;
        this.recordDAO = recordDAO;
        this.entryDAO = entryDAO;
        this.itemDAO = itemDAO;
        this.objectReconstructor = objectReconstructor;
        this.itemValidator = itemValidator;
        this.entryStore = entryStore;
        this.registerPrimaryKey = registerNameConfiguration.getRegister();
    }

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @PermitAll
    @Path("/load")
    @Consumes(MediaType.APPLICATION_JSON)
    public void load(String payload) {
        try {
            Iterable<JsonNode> objects = objectReconstructor.reconstruct(payload.split("\n"));
            objects.forEach(singleObject -> itemValidator.validateItem(registerPrimaryKey, singleObject));
            entryStore.load(registerPrimaryKey, objects);
        } catch (Throwable t) {
            logger.error(Throwables.getStackTraceAsString(t));
            throw t;
        }
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
                .header("Content-Disposition", String.format("attachment; filename=%s-%d.zip", registerPrimaryKey, System.currentTimeMillis()))
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

