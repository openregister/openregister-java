package uk.gov.register.resources.v1;

import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/")
public class RecordResource extends uk.gov.register.resources.v2.RecordResource {
    @Inject
    public RecordResource(RegisterReadOnly register, ViewFactory viewFactory, RequestContext requestContext) {
        super(register, viewFactory, requestContext);
    }
}
