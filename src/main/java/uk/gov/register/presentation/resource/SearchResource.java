package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.SingleEntryView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;
import java.util.function.Function;


@Path("/")
public class SearchResource {

    protected final RequestContext requestContext;
    private final ViewFactory viewFactory;
    private final RecentEntryIndexQueryDAO queryDAO;

    @Inject
    public SearchResource(ViewFactory viewFactory, RequestContext requestContext, RecentEntryIndexQueryDAO queryDAO) {
        this.requestContext = requestContext;
        this.viewFactory = viewFactory;
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("/{key}/{value}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public Object find(@PathParam("key") String key, @PathParam("value") String value) throws Exception {
        if (key.equals(requestContext.getRegisterPrimaryKey())) {
            return entryResponse(queryDAO.findRecordByPrimaryKey(value), viewFactory::getLatestEntryView);
        }


        return Response.status(301).location(URI.create(String.format("/records/%s/%s", key, value))).build();
    }

    private SingleEntryView entryResponse(Optional<DbEntry> optionalEntry, Function<DbEntry, SingleEntryView> convertToEntryView) {
        SingleEntryView singleEntryView = optionalEntry.map(convertToEntryView)
                .orElseThrow(NotFoundException::new);

        requestContext.
                getHttpServletResponse().
                setHeader("Link", String.format("<%s>;rel=\"version-history\"", singleEntryView.getVersionHistoryLink()));

        return singleEntryView;
    }

}
