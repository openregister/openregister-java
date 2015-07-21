package uk.gov.register.presentation.resource;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ListResultView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public class DataResource extends ResourceBase {
    private final RecentEntryIndexQueryDAO queryDAO;

    public DataResource(RecentEntryIndexQueryDAO queryDAO) {
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("/feed")
    @Produces({MediaType.APPLICATION_JSON})
    public List<JsonNode> feed() {
        return queryDAO.getFeeds(ENTRY_LIMIT);
    }

    @GET
    @Path("/all")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV})
    public List<JsonNode> all() {
        return queryDAO.getAllEntries(getRegisterPrimaryKey(), ENTRY_LIMIT);
    }

    @GET
    @Path("/all")
    @Produces()
    public ListResultView allHtml() {
        return new ListResultView("/templates/entries.mustache", all());
    }

}
