package uk.gov.register.resources.v1;

import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.resources.v2.BlobResource;
import uk.gov.register.service.ItemConverter;
import uk.gov.register.views.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/items")
public class ItemResource extends BlobResource {
    @Inject
    public ItemResource(RegisterReadOnly register, ViewFactory viewFactory, ItemConverter itemConverter) {
        super(register, viewFactory, itemConverter);
    }
}
