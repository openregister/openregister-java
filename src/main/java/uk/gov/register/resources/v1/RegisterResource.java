package uk.gov.register.resources.v1;

import uk.gov.register.core.RegisterReadOnly;
import uk.gov.register.views.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/register")
public class RegisterResource extends uk.gov.register.resources.v2.RegisterResource {
    @Inject
    public RegisterResource(RegisterReadOnly register, ViewFactory viewFactory) {
        super(register, viewFactory);
    }
}
