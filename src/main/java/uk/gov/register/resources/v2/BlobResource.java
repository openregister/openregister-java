package uk.gov.register.resources.v2;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.IntParam;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Field;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.providers.params.IntegerParam;
import uk.gov.register.resources.HttpServletResponseAdapter;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.resources.StartLimitPagination;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.v2.BlobListView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;
import uk.gov.register.views.v2.BlobView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/next/blobs")
public class BlobResource {
    protected final RegisterReadOnly register;
    protected final ViewFactory viewFactory;
    protected final ItemConverter itemConverter;
    private final HttpServletResponseAdapter httpServletResponseAdapter;

    @Inject
    public BlobResource(RegisterReadOnly register, ViewFactory viewFactory, ItemConverter itemConverter, RequestContext requestContext) {
        this.register = register;
        this.viewFactory = viewFactory;
        this.itemConverter = itemConverter;
        this.httpServletResponseAdapter = new HttpServletResponseAdapter(requestContext.getHttpServletResponse());
    }

    @GET
    @Path("/sha-256:{blob-hash}")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_CSV,
    })
    @Timed
    public BlobView getBlobDataByHex(@PathParam("blob-hash") String blobHash) throws FieldConversionException {
        return getBlob(blobHash).map(this::buildBlobView)
                .orElseThrow(() -> new NotFoundException("No blob found with blob hash: " + blobHash));
    }

    @GET
    @Path("/")
    @Produces({
            MediaType.APPLICATION_JSON,
            ExtraMediaType.TEXT_CSV,
    })
    @Timed
    public BlobListView listBlobs(@QueryParam("start") Optional<String> optionalStart, @QueryParam("limit") Optional<IntegerParam> optionalLimit) throws FieldConversionException {
        Optional<HashValue> start = optionalStart.map(value -> new HashValue(HashingAlgorithm.SHA256, value));
        int limit =  optionalLimit.map(IntParam::get).orElse(100);

        Collection<Item> items = register.getUserItemsPaginated(start, limit + 1);
        List<Item> itemsList = new ArrayList<>(items);

        boolean hasNext = (items.size() == limit + 1);

        if(hasNext) {
            Item finalBlob = itemsList.get(limit);
            String nextStart = finalBlob.getBlobHash().getValue();
            setNextHeader(nextStart, limit);
        }

        return buildBlobListView(items.stream().limit(limit).collect(Collectors.toList()));
    }

    protected Optional<Item> getBlob(String blobHash) {
        HashValue hash = new HashValue(HashingAlgorithm.SHA256, blobHash);
        return register.getItem(hash);
    }

    protected Map<String, Field> getFieldsByName() {
        return register.getFieldsByName();
    }

    private BlobView buildBlobView(Item item) {
        return new BlobView(item, register.getFieldsByName(), this.itemConverter);
    }

    private BlobListView buildBlobListView(Collection<Item> items) {
        return new BlobListView(items, getFieldsByName());
    }

    private void setNextHeader(String nextStart, int limit) {
        String nextUrl = String.format("?start=%s&limit=%s", nextStart, limit);
        httpServletResponseAdapter.setLinkHeader("next", nextUrl);
    }
}
