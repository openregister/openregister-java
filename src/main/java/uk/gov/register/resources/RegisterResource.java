package uk.gov.register.resources;

import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.RecordQueryDAO;
import uk.gov.register.api.representations.ExtraMediaType;
import uk.gov.register.presentation.view.RegisterDetailView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class RegisterResource {
    protected final ViewFactory viewFactory;
    private RecordQueryDAO recordDAO;
    private final EntryQueryDAO entryDAO;

    @Inject
    public RegisterResource(ViewFactory viewFactory, RecordQueryDAO recordDAO, EntryQueryDAO entryDAO) {
        this.viewFactory = viewFactory;
        this.recordDAO = recordDAO;
        this.entryDAO = entryDAO;
    }

    @GET
    @Path("/register")
    @Produces({MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML})
    public RegisterDetailView getRegisterDetail() {
        return viewFactory.registerDetailView(
                recordDAO.getTotalRecords(),
                entryDAO.getTotalEntries(),
                entryDAO.getTotalEntries(),
                entryDAO.getLastUpdatedTime()
        );
    }
}
