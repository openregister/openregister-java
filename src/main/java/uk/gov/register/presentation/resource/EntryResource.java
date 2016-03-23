package uk.gov.register.presentation.resource;

import io.dropwizard.jersey.caching.CacheControl;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.dao.EntryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.NewEntryView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("/entry")
public class EntryResource {

    private final EntryDAO entryDAO;
    private final ViewFactory viewFactory;

    @Inject
    public EntryResource(EntryDAO entryDAO, ViewFactory viewFactory) {
        this.entryDAO = entryDAO;
        this.viewFactory = viewFactory;
    }

    @GET
    @Path("/{entry-number}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @CacheControl(immutable = true)
    public NewEntryView findByEntryNumber(@PathParam("entry-number") int entryNumber) {
        Optional<Entry> entry = entryDAO.findByEntryNumber(entryNumber);
        return entry.map(viewFactory::getNewEntryView).orElseThrow(NotFoundException::new);
    }
}
