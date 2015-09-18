package uk.gov.register.presentation.resource;

import io.dropwizard.views.View;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.EntryListView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/")
public class DataResource {
    public static final long ENTRY_LIMIT = 100;
    protected final RequestContext requestContext;
    private final RecentEntryIndexQueryDAO queryDAO;

    private final ViewFactory viewFactory;

    @Inject
    public DataResource(ViewFactory viewFactory, RequestContext requestContext, RecentEntryIndexQueryDAO queryDAO) {
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.queryDAO = queryDAO;
    }

    @GET
    @Path("/download")
    @Produces(MediaType.TEXT_HTML)
    public View download() {
        return viewFactory.thymeleafView("download.html");
    }

    @GET
    @Path("/download.torrent")
    @Produces(MediaType.TEXT_HTML)
    public Response downloadTorrent() {
        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .entity(viewFactory.thymeleafView("not-implemented.html"))
                .build();
    }

    @GET
    @Path("/feed")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public EntryListView feed(@QueryParam("pageIndex") Optional<Long> pageIndex, @QueryParam("pageSize") Optional<Long> pageSize) {
        Pagination pagination = new Pagination(pageIndex, pageSize, queryDAO.getEstimatedEntriesCount());

        List<DbEntry> entries = queryDAO.getAllEntries(pagination.pageSize(), pagination.offset());

        EntryListView entryFeedView = viewFactory.getEntryFeedView(entries);
        setNextAndPreviousPageLinkHeader("feed", entryFeedView, pagination);

        return entryFeedView;
    }

    @GET
    @Path("/current")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public EntryListView current() {
        return viewFactory.getRecordEntriesView(queryDAO.getLatestEntriesOfRecords(requestContext.getRegisterPrimaryKey(), ENTRY_LIMIT));
    }

    @GET
    @Path("/all")
    public Response all() {
        return create301Response("current");
    }

    @GET
    @Path("/latest")
    public Response latest() {
        return create301Response("feed");
    }

    private void setNextAndPreviousPageLinkHeader(String resourceMethod, EntryListView entryFeedView, Pagination pagination) {
        List<String> headerValues = new ArrayList<>();

        if (pagination.hasNextPage()) {
            String nextPageLink = nextPageLink(resourceMethod, pagination);

            headerValues.add(String.format("<%s>; rel=\"%s\"", nextPageLink, "next"));
            entryFeedView.setNextPageLink(nextPageLink);
        }

        if (pagination.hasPreviousPage()) {
            String previousLink = String.format(
                    "/%s?pageIndex=%s&pageSize=%s",
                    resourceMethod,
                    pagination.previousPageNumber(),
                    pagination.pageSize()
            );
            headerValues.add(String.format("<%s>; rel=\"%s\"", previousLink, "previous"));
            entryFeedView.setPreviousPageLink(previousLink);
        }

        if (!headerValues.isEmpty()) {
            requestContext.getHttpServletResponse().setHeader("Link", String.join(",", headerValues));
        }
    }

    private String nextPageLink(String resourceMethod, Pagination pagination) {
        return String.format(
                        "/%s?pageIndex=%s&pageSize=%s",
                        resourceMethod,
                        pagination.nextPageNumber(),
                        pagination.pageSize()
                );
    }

    private Response create301Response(String locationMethod) {
        String requestURI = requestContext.requestURI();
        String representation = requestURI.substring(requestURI.lastIndexOf("/")).replaceAll("[^\\.]+(.*)", "$1");

        URI uri = UriBuilder
                .fromUri(requestURI)
                .replacePath(null)
                .path(locationMethod + representation)
                .build();

        return Response
                .status(Response.Status.MOVED_PERMANENTLY)
                .location(uri)
                .build();
    }

}
