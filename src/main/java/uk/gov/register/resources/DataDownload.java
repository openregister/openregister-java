package uk.gov.register.resources;

import io.dropwizard.views.View;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.configuration.ResourceConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterDetail;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.util.ArchiveCreator;
import uk.gov.register.util.TSVFormatter;
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
import java.util.Iterator;

@Path("/")
public class DataDownload {

    private final RegisterReadOnly register;
    protected final ViewFactory viewFactory;
    private String registerPrimaryKey;
    private final ResourceConfiguration resourceConfiguration;

    @Inject
    public DataDownload(RegisterReadOnly register, ViewFactory viewFactory, RegisterNameConfiguration registerNameConfiguration, ResourceConfiguration resourceConfiguration) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.registerPrimaryKey = registerNameConfiguration.getRegisterName();
        this.resourceConfiguration = resourceConfiguration;
    }

    @GET
    @Path("/download-register")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, ExtraMediaType.TEXT_HTML})
    @DownloadNotAvailable
    public Response downloadRegister() {
        Collection<Entry> entries = register.getAllEntries();
        Collection<Item> items = register.getAllItems();

        int totalEntries = register.getTotalEntries();
        int totalRecords = register.getTotalRecords();

        RegisterDetail registerDetail = viewFactory.registerDetailView(
                totalRecords,
                totalEntries,
                register.getLastUpdatedTime()
        ).getRegisterDetail();

        return Response
                .ok(new ArchiveCreator().create(registerDetail, entries, items))
                .header("Content-Disposition", String.format("attachment; filename=%s-%d.zip", registerPrimaryKey, System.currentTimeMillis()))
                .header("Content-Transfer-Encoding", "binary")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                .build();
    }

    @GET
    @Path("/download-rsf")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, ExtraMediaType.TEXT_HTML})
    public Response downloadRSF() {
        int totalEntries = register.getTotalEntries();
        Iterator<Item> itemIterator = register.getItemIterator(0, totalEntries);
        Iterator<Entry> entryIterator = register.getEntryIterator(0, totalEntries);

        return Response
                .ok(new ArchiveCreator().createRSF(itemIterator, entryIterator, new TSVFormatter()))
                .header("Content-Disposition", String.format("attachment; filename=%s-%d.tsv", registerPrimaryKey, System.currentTimeMillis()))
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

