package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.views.View;
import uk.gov.register.core.BaseEntry;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.Collection;
import java.util.Collections;

@Path("/")
public class PreviewEntriesResource {

    private static final int START = 1;

    protected final ViewFactory viewFactory;
    private final RegisterReadOnly register;

    @Inject
    public PreviewEntriesResource(final ViewFactory viewFactory, final RegisterReadOnly register) {
        this.viewFactory = viewFactory;
        this.register = register;
    }

    @GET
    @Path("/preview-entries/{media-type}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public View preview(@PathParam("media-type") final String mediaType) {
        final int limit = register.getTotalEntries() > IndexSizePagination.ENTRY_LIMIT
                ? IndexSizePagination.ENTRY_LIMIT
                : register.getTotalEntries();
        final Collection<BaseEntry> entries = register.getEntries(START, limit);

        return viewFactory.previewEntriesPageView(entries, null, ExtraMediaType.transform(mediaType));
    }

    @GET
    @Path("/preview-entries/{media-type}/{entry-key}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public View jsonByKeyPreview(@PathParam("media-type") final String mediaType, @PathParam("entry-key") final int key) {
        final Collection<BaseEntry> entries = Collections.singletonList(register.getEntry(key).orElseThrow(NotFoundException::new));

        return viewFactory.previewEntriesPageView(entries, key, ExtraMediaType.transform(mediaType));
    }
}
