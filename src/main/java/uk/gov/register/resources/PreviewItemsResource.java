package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.views.View;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;

@Path("/")
public class PreviewItemsResource {

    protected final ViewFactory viewFactory;
    private final RegisterReadOnly register;

    @Inject
    public PreviewItemsResource(final ViewFactory viewFactory, final RegisterReadOnly register) {
        this.viewFactory = viewFactory;
        this.register = register;
    }

    @GET
    @Path("/preview-items/{media-type}/sha-256:{item-hash}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public View jsonByKeyPreview(@PathParam("media-type") final String mediaType, @PathParam("item-hash") final String itemHash) {
        final HashValue hash = new HashValue(HashingAlgorithm.SHA256, itemHash);
        final Item item = register.getItemBySha256(hash)
                .orElseThrow(() -> new NotFoundException("No item found with item hash: " + itemHash));

        return viewFactory.previewItemPageView(item, itemHash, ExtraMediaType.transform(mediaType));
    }
}
