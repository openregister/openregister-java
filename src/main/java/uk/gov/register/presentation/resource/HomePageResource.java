package uk.gov.register.presentation.resource;

import io.dropwizard.views.View;
import uk.gov.register.presentation.dao.EntryDAO;
import uk.gov.register.presentation.dao.RecordDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class HomePageResource {
    private final ViewFactory viewFactory;
    private final RecordDAO recordDAO;
    private EntryDAO entryDAO;

    @Inject
    public HomePageResource(ViewFactory viewFactory, RecordDAO recordDAO, EntryDAO entryDAO) {
        this.viewFactory = viewFactory;
        this.recordDAO = recordDAO;
        this.entryDAO = entryDAO;
    }

    @GET
    @Produces({ExtraMediaType.TEXT_HTML})
    public View home() {
        return viewFactory.homePageView(recordDAO.getTotalRecords(), entryDAO.getTotalEntries(), entryDAO.getLastUpdatedTime());
    }

    @GET
    @Path("/robots.txt")
    @Produces(MediaType.TEXT_PLAIN)
    public String robots() {
        return "User-agent: *\n" +
                "Disallow: /\n";
    }
}
