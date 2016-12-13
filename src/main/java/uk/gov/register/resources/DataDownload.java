package uk.gov.register.resources;

import io.dropwizard.views.View;
import uk.gov.register.configuration.ResourceConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterDetail;
import uk.gov.register.core.RegisterName;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.serialization.RegisterSerialisationFormat;
import uk.gov.register.service.RegisterSerialisationFormatService;
import uk.gov.register.util.ArchiveCreator;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/")
public class DataDownload {

    private final RegisterReadOnly register;
    protected final ViewFactory viewFactory;
    private RegisterName registerPrimaryKey;
    private final ResourceConfiguration resourceConfiguration;
    private RegisterSerialisationFormatService rsfService;

    @Inject
    public DataDownload(RegisterReadOnly register, ViewFactory viewFactory, ResourceConfiguration resourceConfiguration, RegisterSerialisationFormatService rsfService) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.registerPrimaryKey = register.getRegisterName();
        this.resourceConfiguration = resourceConfiguration;
        this.rsfService = rsfService;
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
    @Produces({ExtraMediaType.APPLICATION_RSF, ExtraMediaType.TEXT_HTML})
    @DownloadNotAvailable
    public RegisterSerialisationFormat downloadRSF() {
        return rsfService.createRegisterSerialisationFormat();
    }

    @GET
    @Path("/download-rsf/{total-entries-1}/{total-entries-2}")
    @Produces({ExtraMediaType.APPLICATION_RSF, ExtraMediaType.TEXT_HTML})
    @DownloadNotAvailable
    public RegisterSerialisationFormat downloadPartialRSF(@PathParam("total-entries-1") int totalEntries1, @PathParam("total-entries-2") int totalEntries2) {
        if (totalEntries1 < 0) {
            throw new BadRequestException("total-entries-1 must be 0 or greater");
        }

        if (totalEntries2 < totalEntries1) {
            throw new BadRequestException("total-entries-2 must be greater than or equal to total-entries-1");
        }

        int totalEntriesInRegister = register.getTotalEntries();

        if (totalEntries2 > totalEntriesInRegister){
            throw new BadRequestException("total-entries-2 must not exceed number of total entries in the register");
        }

        return rsfService.createRegisterSerialisationFormat(totalEntries1, totalEntries2);
    }

    @GET
    @Path("/download")
    @Produces(ExtraMediaType.TEXT_HTML)
    public View download() {
        return viewFactory.downloadPageView(resourceConfiguration.getEnableDownloadResource());
    }
}

