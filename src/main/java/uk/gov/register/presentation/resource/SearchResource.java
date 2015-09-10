package uk.gov.register.presentation.resource;

import com.google.common.base.Optional;
import uk.gov.register.presentation.DbRecord;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.ListResultView;
import uk.gov.register.presentation.view.SingleResultView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;


@Path("/")
public class SearchResource {

    public static final int ENTRY_LIMIT = 100;
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
    @Path("search")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public ListResultView search(@Context UriInfo uriInfo) {
        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        return viewFactory.getListResultView(
                queryParameters.entrySet()
                        .stream()
                        .findFirst()
                        .map(e -> queryDAO.findAllByKeyValue(e.getKey(), e.getValue().get(0)))
                        .orElseGet(() -> queryDAO.getAllRecords(requestContext.getRegisterPrimaryKey(), ENTRY_LIMIT))
        );
    }

    @GET
    @Path("/{primaryKey}/{primaryKeyValue}")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public SingleResultView findByPrimaryKey(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) {
        String registerPrimaryKey = requestContext.getRegisterPrimaryKey();
        if (key.equals(registerPrimaryKey)) {
            Optional<DbRecord> record = queryDAO.findByKeyValue(key, value);
            if (record.isPresent()) {
                setResponseHeader(key, value);
                return viewFactory.getSingleResultView(record.get());
            }
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/hash/{hash}")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public SingleResultView findByHash(@PathParam("hash") String hash) {
        Optional<DbRecord> optionalRecord = queryDAO.findByHash(hash);
        if (optionalRecord.isPresent()) {
            DbRecord record = optionalRecord.get();

            String primaryKey = requestContext.getRegisterPrimaryKey();
            setResponseHeader(primaryKey, record.getEntry().get(primaryKey).textValue());

            return viewFactory.getSingleResultView(record);
        }
        throw new NotFoundException();
    }

    private void setResponseHeader(String key, String value) {
        requestContext.
                getHttpServletResponse().
                setHeader("Link", String.format("</%s/%s/history>;rel=version-history", key, value));
    }
}
