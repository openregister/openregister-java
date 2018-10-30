package uk.gov.register.resources.v1;

import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterReadOnly;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/")
public class SearchResource extends uk.gov.register.resources.v2.SearchResource {
    @Inject
    public SearchResource(RegisterId registerId, RegisterReadOnly register) {
        super(registerId, register);
    }
}
