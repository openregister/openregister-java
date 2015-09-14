package uk.gov.register.presentation.resource;

import uk.gov.register.presentation.DbRecord;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.SingleResultView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;


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
    @Path("/{primaryKey}/{primaryKeyValue}")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public SingleResultView findByPrimaryKey(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) {
        if (!key.equals(requestContext.getRegisterPrimaryKey())) {
            throw new NotFoundException();
        }
        Optional<DbRecord> recordO = queryDAO.findByKeyValue(key, value);
        return recordResponse(recordO);
    }

    @GET
    @Path("/hash/{hash}")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public SingleResultView findByHash(@PathParam("hash") String hash) {
        Optional<DbRecord> recordO = queryDAO.findByHash(hash);
        return recordResponse(recordO);
    }

    @GET
    @Path("/entry/{serial}")
    public SingleResultView findBySerial(@PathParam("serial") int serial) {
        Optional<DbRecord> recordO = queryDAO.findBySerial(serial);
        return recordResponse(recordO);
    }

    private SingleResultView recordResponse(Optional<DbRecord> optionalRecord) {
        optionalRecord.ifPresent(this::setVersionHistoryLinkHeader);
        return optionalRecord.map(viewFactory::getSingleResultView)
                .orElseThrow(NotFoundException::new);
    }

    private void setVersionHistoryLinkHeader(DbRecord record) {
        String primaryKey = requestContext.getRegisterPrimaryKey();
        requestContext.
                getHttpServletResponse().
                setHeader("Link", String.format("</%s/%s/history>;rel=\"version-history\"",
                        primaryKey,
                        record.getEntry().get(primaryKey).textValue()));
    }
}
