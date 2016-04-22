package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.dao.EntryDAO;
import uk.gov.register.presentation.dao.RecordDAO;
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
    private RecordDAO recordDAO;
    private final EntryDAO entryDAO;

    @Inject
    public RegisterResource(ViewFactory viewFactory, RecordDAO recordDAO, EntryDAO entryDAO) {
        this.viewFactory = viewFactory;
        this.recordDAO = recordDAO;
        this.entryDAO = entryDAO;
    }

    @GET
    @Path("/register")
    @Produces({MediaType.APPLICATION_JSON})
    public RegisterDetailView getRegisterDetail() {
        return viewFactory.registerDetailView(
                recordDAO.getTotalRecords(),
                entryDAO.getTotalEntries(),
                entryDAO.getTotalEntries(),
                entryDAO.getLastUpdatedTime()
        );
    }
}
