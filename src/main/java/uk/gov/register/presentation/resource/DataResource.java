package uk.gov.register.presentation.resource;

import io.dropwizard.views.View;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import uk.gov.register.presentation.ArchiveCreator;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.RegisterDetail;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.dao.SignedTreeHeadQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.EntryListView;
import uk.gov.register.presentation.view.ViewFactory;
import uk.gov.register.proofs.ct.SignedTreeHead;
import uk.gov.register.presentation.view.RegisterDetailView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/")
public class DataResource {
    protected final RequestContext requestContext;
    private final RecentEntryIndexQueryDAO queryDAO;
    private final SignedTreeHeadQueryDAO signedTreeHeadQueryDAO;

    private final ViewFactory viewFactory;

    @Inject
    public DataResource(ViewFactory viewFactory, RequestContext requestContext, RecentEntryIndexQueryDAO queryDAO, SignedTreeHeadQueryDAO signedTreeHeadQueryDAO) {
        this.viewFactory = viewFactory;
        this.requestContext = requestContext;
        this.queryDAO = queryDAO;
        this.signedTreeHeadQueryDAO = signedTreeHeadQueryDAO;
    }

    @GET
    @Path("/download-register")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadRegister() {
        List<DbEntry> entries = queryDAO.getAllEntriesNoPagination();
        SignedTreeHead sth = signedTreeHeadQueryDAO.get();

        RegisterDetail registerDetail = viewFactory.registerDetailView(
                queryDAO.getTotalRecords(),
                queryDAO.getTotalEntries(),
                queryDAO.getTotalEntries(),
                queryDAO.getLastUpdatedTime()
        ).getRegisterDetail();

        return Response
                .ok(new ArchiveCreator().create(registerDetail, entries, sth))
                .header("Content-Disposition", String.format("attachment; filename=%s-%d.zip", requestContext.getRegisterPrimaryKey(), System.currentTimeMillis()))
                .header("Content-Transfer-Encoding", "binary")
                .build();
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
        Pagination pagination = new Pagination(pageIndex, pageSize, queryDAO.getTotalEntries());

        setNextAndPreviousPageLinkHeader(pagination);

        getFileExtension().ifPresent(ext -> addContentDispositionHeader(requestContext.getRegisterPrimaryKey() + "-entries." + ext));
        return viewFactory.getEntriesView(queryDAO.getAllEntries(pagination.pageSize(), pagination.offset()), pagination);
    }

    @GET
    @Path("/records")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public EntryListView records(@QueryParam(Pagination.INDEX_PARAM) Optional<Long> pageIndex, @QueryParam(Pagination.SIZE_PARAM) Optional<Long> pageSize) {
        Pagination pagination = new Pagination(pageIndex, pageSize, queryDAO.getTotalRecords());

        setNextAndPreviousPageLinkHeader(pagination);

        getFileExtension().ifPresent(ext -> addContentDispositionHeader(requestContext.getRegisterPrimaryKey() + "-records." + ext));
        return viewFactory.getRecordsView(queryDAO.getLatestEntriesOfRecords(pagination.pageSize(), pagination.offset()), pagination);
    }

    @GET
    @Path("/register")
    @Produces({MediaType.APPLICATION_JSON})
    public RegisterDetailView getRegisterDetail() {
        return viewFactory.registerDetailView(queryDAO.getTotalRecords(), queryDAO.getTotalEntries(), queryDAO.getTotalEntries(), queryDAO.getLastUpdatedTime());
    }

    private Optional<String> getFileExtension() {
        String requestURI = requestContext.getHttpServletRequest().getRequestURI();
        if (requestURI.lastIndexOf('.') == -1) {
            return Optional.empty();
        }
        String[] tokens = requestURI.split("\\.");
        return Optional.of(tokens[tokens.length - 1]);
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
}
