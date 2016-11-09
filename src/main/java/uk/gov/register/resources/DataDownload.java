package uk.gov.register.resources;

import io.dropwizard.views.View;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.configuration.ResourceConfiguration;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterDetail;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.serialization.AddItemCommand;
import uk.gov.register.serialization.AppendEntryCommand;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.util.ArchiveCreator;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
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
    @Produces({ExtraMediaType.RSF, ExtraMediaType.TEXT_HTML})
    @DownloadNotAvailable
    public Iterator<RegisterCommand> downloadRSF() {
        Iterator<RegisterCommand> itemCommandsIterator = Iterators.transform(register.getItemIterator(), AddItemCommand::new);
        Iterator<RegisterCommand> entryCommandIterator = Iterators.transform(register.getEntryIterator(), AppendEntryCommand::new);

        return Iterators.concat(itemCommandsIterator, entryCommandIterator);
    }

    @GET
    @Path("/download-rsf/{start-entry-no}/{end-entry-no}")
    @Produces({ExtraMediaType.RSF, ExtraMediaType.TEXT_HTML})
    @DownloadNotAvailable
    public Iterator<RegisterCommand> downloadPartialRSF(@PathParam("start-entry-no") int startEntryNo, @PathParam("end-entry-no") int endEntryNo) {

        if (startEntryNo > endEntryNo) {
            throw new BadRequestException("start-entry-no must be smaller than or equal to end-entry-no");
        }

        int totalEntries = register.getTotalEntries();
        if(startEntryNo > totalEntries){
            throw new BadRequestException("start-entry-no must be smaller than total entries in the register");
        }

        Iterator<RegisterCommand> itemCommandsIterator = Iterators.transform(register.getItemIterator(startEntryNo, endEntryNo), AddItemCommand::new);
        Iterator<RegisterCommand> entryCommandIterator = Iterators.transform(register.getEntryIterator(startEntryNo, endEntryNo), AppendEntryCommand::new);

        return Iterators.concat(itemCommandsIterator, entryCommandIterator);
    }

    @GET
    @Path("/download")
    @Produces(ExtraMediaType.TEXT_HTML)
    public View download() {
        return viewFactory.downloadPageView(resourceConfiguration.getEnableDownloadResource());
    }
}

