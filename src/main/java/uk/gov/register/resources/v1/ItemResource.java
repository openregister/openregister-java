package uk.gov.register.resources.v1;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.exceptions.FieldConversionException;
import uk.gov.register.resources.v2.BlobResource;
import uk.gov.register.views.AttributionView;
import uk.gov.register.views.ItemView;
import uk.gov.register.views.ViewFactory;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/items")
public class ItemResource extends BlobResource {
    @Inject
    public ItemResource(RegisterReadOnly register, ViewFactory viewFactory) {
        super(register, viewFactory);
    }
}
