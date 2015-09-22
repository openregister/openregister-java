package uk.gov.register.presentation.resource;

import com.google.common.primitives.Ints;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.SingleEntryView;
import uk.gov.register.presentation.view.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
    @Path("/{primaryKey}/{primaryKeyValue}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public SingleEntryView findByPrimaryKey(@PathParam("primaryKey") String key, @PathParam("primaryKeyValue") String value) {
        if (!key.equals(requestContext.getRegisterPrimaryKey())) {
            throw new NotFoundException();
        }
        Optional<DbEntry> entryO = queryDAO.findByKeyValue(key, value);
        return entryResponse(entryO, viewFactory::getLatestEntryView);
    }

    @GET
    @Path("/hash/{hash}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public SingleEntryView findByHash(@PathParam("hash") String hash) {
        Optional<DbEntry> entryO = queryDAO.findByHash(hash);
        return entryResponse(entryO, viewFactory::getSingleEntryView);
    }

    @GET
    @Path("/entry/{serial}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    public SingleEntryView findBySerial(@PathParam("serial") String serialString) {
        Optional<Integer> serial = Optional.ofNullable(Ints.tryParse(serialString));
        Optional<DbEntry> entryO = serial.flatMap(queryDAO::findBySerial);
        return entryResponse(entryO, viewFactory::getSingleEntryView);
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
