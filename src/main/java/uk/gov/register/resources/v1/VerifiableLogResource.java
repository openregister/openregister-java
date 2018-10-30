package uk.gov.register.resources.v1;

import uk.gov.register.core.RegisterReadOnly;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/proof")
public class VerifiableLogResource extends uk.gov.register.resources.v2.VerifiableLogResource {
    @Inject
    public VerifiableLogResource(RegisterReadOnly register) {
        super(register);
    }
}
