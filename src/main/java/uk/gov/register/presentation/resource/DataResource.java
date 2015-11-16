package uk.gov.register.presentation.resource;

import io.dropwizard.views.View;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.EntryListView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;
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

        getFileExtension().ifPresent(ext -> addContentDispositionHeader(requestContext.getRegisterPrimaryKey() + "-entries." + ext));
        return viewFactory.getEntryFeedView(queryDAO.getAllEntries(pagination.pageSize(), pagination.offset()), pagination);
    }

    @GET
    @Path("/records")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public EntryListView records(@QueryParam(Pagination.INDEX_PARAM) Optional<Long> pageIndex, @QueryParam(Pagination.SIZE_PARAM) Optional<Long> pageSize) {
        Pagination pagination = new Pagination("/records", pageIndex, pageSize, queryDAO.getTotalRecords());

        setNextAndPreviousPageLinkHeader(pagination);

        getFileExtension().ifPresent(ext -> addContentDispositionHeader(requestContext.getRegisterPrimaryKey() + "-records." + ext));
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

    private Optional<String> getFileExtension() {
        String requestURI = requestContext.getHttpServletRequest().getRequestURI();
        if (requestURI.lastIndexOf('.') == -1) {
            return Optional.empty();
        }
        String[] tokens = requestURI.split("\\.");
        return Optional.of(tokens[tokens.length-1]);
    }

    private void addContentDispositionHeader(String fileName) {
        ContentDisposition contentDisposition = ContentDisposition.type("attachment").fileName(fileName).build();
        requestContext.getHttpServletResponse().addHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
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

    private Response create301Response(String path, Optional<Long> pageIndex, Optional<Long> pageSize) {
        String requestURI = requestContext.requestURI();
        String representation = requestURI.substring(requestURI.lastIndexOf("/")).replaceAll("[^\\.]+(.*)", "$1");

        UriBuilder builder = UriBuilder
                .fromUri(requestURI)
                .replacePath(null)
                .path(path + representation);

        if (pageIndex.isPresent()) {
            builder = builder.queryParam(Pagination.INDEX_PARAM, pageIndex.get());
        }
        if (pageSize.isPresent()) {
            builder = builder.queryParam(Pagination.SIZE_PARAM, pageSize.get());
        }

        return Response
                .status(Response.Status.MOVED_PERMANENTLY)
                .location(builder.build())
                .build();
    }

}
