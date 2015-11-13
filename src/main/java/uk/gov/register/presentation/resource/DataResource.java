package uk.gov.register.presentation.resource;

import io.dropwizard.views.View;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/")
public class DataResource {
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
    @Produces(ExtraMediaType.TEXT_HTML)
    public View download() {
        return viewFactory.thymeleafView("download.html");
    }

    @GET
    @Path("/download.torrent")
    @Produces(ExtraMediaType.TEXT_HTML)
    public Response downloadTorrent() {
        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .entity(viewFactory.thymeleafView("not-implemented.html"))
                .build();
    }

    @GET
    @Path("/entries")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public EntryListView entries(@QueryParam(Pagination.INDEX_PARAM) Optional<Long> pageIndex, @QueryParam(Pagination.SIZE_PARAM) Optional<Long> pageSize) {
        Pagination pagination = new Pagination("/entries", pageIndex, pageSize, queryDAO.getTotalEntriesCount());

        setNextAndPreviousPageLinkHeader(pagination);

        return viewFactory.getEntryFeedView(queryDAO.getAllEntries(pagination.pageSize(), pagination.offset()), pagination);
    }

    @GET
    @Path("/records")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public EntryListView records(@QueryParam(Pagination.INDEX_PARAM) Optional<Long> pageIndex, @QueryParam(Pagination.SIZE_PARAM) Optional<Long> pageSize) {
        Pagination pagination = new Pagination("/records", pageIndex, pageSize, queryDAO.getTotalRecords());

        setNextAndPreviousPageLinkHeader(pagination);

        return viewFactory.getRecordEntriesView(queryDAO.getLatestEntriesOfRecords(pagination.pageSize(), pagination.offset()), pagination);
    }

    @GET
    @Path("/feed")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public Response feed(@QueryParam("pageIndex") Optional<Long> pageIndex, @QueryParam("pageSize") Optional<Long> pageSize) {
        return create301Response("/entries", pageIndex, pageSize);
    }

    @GET
    @Path("/current")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public Response current(@QueryParam("pageIndex") Optional<Long> pageIndex, @QueryParam("pageSize") Optional<Long> pageSize) {
        return create301Response("/records", pageIndex, pageSize);
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

    private void setNextAndPreviousPageLinkHeader(Pagination pagination) {
        List<String> headerValues = new ArrayList<>();

        if (pagination.hasNextPage()) {
            headerValues.add(String.format("<%s>; rel=\"%s\"", pagination.getNextPageLink(), "next"));
        }

        if (pagination.hasPreviousPage()) {
            headerValues.add(String.format("<%s>; rel=\"%s\"", pagination.getPreviousPageLink(), "previous"));
        }

        if (!headerValues.isEmpty()) {
            requestContext.getHttpServletResponse().setHeader("Link", String.join(",", headerValues));
        }
    }

    private Response create301Response(String path) {
        return create301Response(path, Collections.emptyMap());
    }

    private Response create301Response(String path, Optional<Long> pageIndex, Optional<Long> pageSize) {
        HashMap<String, Long> queryParams = new HashMap<>();
        pageIndex.ifPresent(i -> queryParams.put(Pagination.INDEX_PARAM, i));
        pageSize.ifPresent(s -> queryParams.put(Pagination.SIZE_PARAM, s));
        return create301Response(path, queryParams);
    }

    private Response create301Response(String path, Map<String, ?> queryParams) {
        String requestURI = requestContext.requestURI();
        String representation = requestURI.substring(requestURI.lastIndexOf("/")).replaceAll("[^\\.]+(.*)", "$1");

        UriBuilder builder = UriBuilder
                .fromUri(requestURI)
                .replacePath(null)
                .path(path + representation);

        for (Map.Entry<String, ?> queryParam : queryParams.entrySet()) {
            builder = builder.queryParam(queryParam.getKey(), queryParam.getValue());
        }

        return Response
                .status(Response.Status.MOVED_PERMANENTLY)
                .location(builder.build())
                .build();
    }

}
