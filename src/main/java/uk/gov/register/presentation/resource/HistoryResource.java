package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.Version;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ListVersionView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.stream.Collectors;

@Path("/")
public class HistoryResource {
    private final RequestContext requestContext;
    private final RecentEntryIndexQueryDAO queryDAO;
    private final ViewFactory viewFactory;

    @Inject
    public HistoryResource(RequestContext requestContext, RecentEntryIndexQueryDAO queryDAO, ViewFactory viewFactory) {
        this.requestContext = requestContext;
        this.queryDAO = queryDAO;
        this.viewFactory = viewFactory;
    }

    @GET
    @Path("/{primaryKey}/{primaryKeyValue}/history")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    public ListVersionView history(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) throws Exception {
        String registerPrimaryKey = requestContext.getRegisterPrimaryKey();
        if (key.equals(registerPrimaryKey)) {
            return viewFactory.listVersionView(queryDAO.findAllByKeyValue(key, value).stream().map(r -> new Version(r.getSerialNumber(), r.getContent().getHash())).collect(Collectors.toList()));
        }
        throw new NotFoundException();
    }

}
