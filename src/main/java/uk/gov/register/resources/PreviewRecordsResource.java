package uk.gov.register.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.views.View;
import uk.gov.register.core.Record;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.Collections;
import java.util.List;

@Path("/")
public class PreviewRecordsResource {

    private static final int DEFAULT_OFFSET = 0;

    protected final ViewFactory viewFactory;
    private final RegisterReadOnly register;

    @Inject
    public PreviewRecordsResource(final ViewFactory viewFactory, final RegisterReadOnly register) {
        this.viewFactory = viewFactory;
        this.register = register;
    }

    @GET
    @Path("/preview-records/{media-type}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public View jsonPreview(@PathParam("media-type") final String mediaType) {
        final int limit = register.getTotalRecords() > IndexSizePagination.ENTRY_LIMIT
                ? IndexSizePagination.ENTRY_LIMIT
                : register.getTotalRecords();
        final List<Record> records = register.getRecords(limit, DEFAULT_OFFSET);

        return viewFactory.previewRecordsPageView(records, null, ExtraMediaType.transform(mediaType));
    }

    @GET
    @Path("/preview-records/{media-type}/{record-key}")
    @Produces(ExtraMediaType.TEXT_HTML)
    @Timed
    public View jsonByKeyPreview(@PathParam("media-type") final String mediaType, @PathParam("record-key") final String key) {
        final List<Record> records = Collections.singletonList(register.getRecord(key)
                .orElseThrow(() -> new NotFoundException("No records found with key: " + key)));

        return viewFactory.previewRecordsPageView(records, key, ExtraMediaType.transform(mediaType));
    }
}
