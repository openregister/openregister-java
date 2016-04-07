package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.dao.RecordDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.RecordView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public class RecordResource {
    private ViewFactory viewFactory;
    private RecordDAO recordDAO;

    @Inject
    public RecordResource(ViewFactory viewFactory, RecordDAO recordDAO) {
        this.viewFactory = viewFactory;
        this.recordDAO = recordDAO;
    }

    @GET
    @Path("/record/{record-key}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    public RecordView getRecordByKey(@PathParam("record-key") String key) {
        return recordDAO
                .findBy(key)
                .map(record -> viewFactory.getRecordView(record))
                .orElseThrow(NotFoundException::new);
    }

}
