package uk.gov.register.resources.v1;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.RegisterDetailView;
import uk.gov.register.views.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/register")
public class RegisterResource {
    private final RegisterReadOnly register;
    protected final ViewFactory viewFactory;

    @Inject
    public RegisterResource(RegisterReadOnly register, ViewFactory viewFactory) {
        this.register = register;
        this.viewFactory = viewFactory;
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public RegisterDetailView getRegisterDetail() {
        return viewFactory.registerDetailView(
                register.getTotalRecords(EntryType.user),
                register.getTotalEntries(EntryType.user),
                register.getLastUpdatedTime(),
                register.getCustodianName()
        );
    }}
