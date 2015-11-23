package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.register.presentation.Version;
import uk.gov.register.presentation.config.PublicBodiesConfiguration;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/")
public class HistoryResource {
    private final RequestContext requestContext;
    private final RecentEntryIndexQueryDAO queryDAO;
    private final PublicBodiesConfiguration publicBodiesConfiguration;

    @Inject
    public HistoryResource(RequestContext requestContext, RecentEntryIndexQueryDAO queryDAO, PublicBodiesConfiguration publicBodiesConfiguration) {
        this.requestContext = requestContext;
        this.queryDAO = queryDAO;
        this.publicBodiesConfiguration = publicBodiesConfiguration;
    }

    @GET
    @Path("/{primaryKey}/{primaryKeyValue}/history")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    public ListVersionView history(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) throws Exception {
        String registerPrimaryKey = requestContext.getRegisterPrimaryKey();
        if (key.equals(registerPrimaryKey)) {
            return new ListVersionView(requestContext, publicBodiesConfiguration, queryDAO.findAllByKeyValue(key, value).stream().map(r -> new Version(r.getSerialNumber(), r.getContent().getHash())).collect(Collectors.toList()));
        }
        throw new NotFoundException();
    }

    public static class ListVersionView extends ThymeleafView {
        private final List<Version> versions;

        public ListVersionView(RequestContext requestContext, PublicBodiesConfiguration publicBodiesConfiguration, List<Version> versions) {
            super(requestContext, publicBodiesConfiguration, "history.html");
            this.versions = versions;
        }

        @JsonValue
        public List<Version> getVersions() {
            return versions;
        }
    }
}
