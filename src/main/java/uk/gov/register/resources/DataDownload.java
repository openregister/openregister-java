package uk.gov.register.resources;

import io.dropwizard.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.configuration.ResourceConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterDetail;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.util.ArchiveCreator;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/")
public class DataDownload {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final ViewFactory viewFactory;
    private final ItemQueryDAO itemDAO;
    private final RecordQueryDAO recordDAO;
    private EntryQueryDAO entryDAO;
    private String registerPrimaryKey;
    private final ResourceConfiguration resourceConfiguration;

    @Inject
    public DataDownload(ViewFactory viewFactory, RecordQueryDAO recordDAO, EntryQueryDAO entryDAO, ItemQueryDAO itemDAO, RegisterNameConfiguration registerNameConfiguration, ResourceConfiguration resourceConfiguration) {
        this.viewFactory = viewFactory;
        this.recordDAO = recordDAO;
        this.entryDAO = entryDAO;
        this.itemDAO = itemDAO;
        this.registerPrimaryKey = registerNameConfiguration.getRegister();
        this.resourceConfiguration = resourceConfiguration;
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
    public View download() {
        return viewFactory.downloadPageView(resourceConfiguration.getEnableDownloadResource());
    }
}

