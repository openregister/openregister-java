package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.register.presentation.Version;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Path("/")
public class HistoryResource {
    private final RequestContext requestContext;
    private final RecentEntryIndexQueryDAO queryDAO;

    @Inject
    public HistoryResource(RequestContext requestContext, RecentEntryIndexQueryDAO queryDAO) {
        this.requestContext = requestContext;
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("/{primaryKey}/{primaryKeyValue}/history")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    public ListVersionView history(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) {
        String registerPrimaryKey = requestContext.getRegisterPrimaryKey();
        if (key.equals(registerPrimaryKey)) {
            return new ListVersionView(requestContext, queryDAO.findAllByKeyValue(key, value).stream().map(r -> new Version(r.getSerialNumber(), r.getContent().getHash())).collect(Collectors.toList()));
        }
        throw new NotFoundException();
    }

    @JsonSerialize(using = ListVersionViewJsonSerializer.class)
    public static class ListVersionView extends ThymeleafView {
        private final List<Version> versions;

        public ListVersionView(RequestContext requestContext, List<Version> versions) {
            super(requestContext, "versions.html");
            this.versions = versions;
        }

        public List<Version> getVersions() {
            return versions;
        }
    }

    private static class ListVersionViewJsonSerializer extends JsonSerializer<ListVersionView> {
        @Override
        public void serialize(ListVersionView listVersion, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            JsonSerializer<Object> listSerializer = serializers.findValueSerializer(List.class);
            listSerializer.serialize(listVersion.getVersions(), gen, serializers);
        }
    }
}
