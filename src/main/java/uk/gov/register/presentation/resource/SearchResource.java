package uk.gov.register.presentation.resource;

import com.google.common.primitives.Ints;
import io.dropwizard.jersey.caching.CacheControl;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.dao.RecentEntryIndexQueryDAO;
import uk.gov.register.presentation.representations.ExtraMediaType;
import uk.gov.register.presentation.view.SingleEntryView;
import uk.gov.register.presentation.view.ViewFactory;
import uk.gov.register.thymeleaf.ThymeleafView;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    public ThymeleafView find(@PathParam("key") String key, @PathParam("value") String value) throws Exception {

        List<DbEntry> records = queryDAO.findLatestEntriesOfRecordsByKeyValue(key, value);

        if (key.equals(requestContext.getRegisterPrimaryKey())) {
            return entryResponse(records.stream().findFirst(), viewFactory::getLatestEntryView);
        }

        Pagination pagination = new Pagination(Optional.empty(), Optional.empty(), 0);
        return viewFactory.getRecordsView(records, pagination);
    }

    @GET
    @Path("/hash/{hash}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    @CacheControl(immutable = true)
    public SingleEntryView findByHash(@PathParam("hash") String hash) {
        Optional<DbEntry> entryO = queryDAO.findEntryByHash(hash);
        return entryResponse(entryO, viewFactory::getSingleEntryView);
    }

    @GET
    @Path("/entry/{serial}")
    @Produces({ExtraMediaType.TEXT_HTML, MediaType.APPLICATION_JSON, ExtraMediaType.TEXT_YAML, ExtraMediaType.TEXT_CSV, ExtraMediaType.TEXT_TSV, ExtraMediaType.TEXT_TTL})
    @CacheControl(immutable = true)
    public SingleEntryView findBySerial(@PathParam("serial") String serialString) {
        Optional<Integer> serial = Optional.ofNullable(Ints.tryParse(serialString));
        Optional<DbEntry> entryO = serial.flatMap(queryDAO::findEntryBySerialNumber);
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
