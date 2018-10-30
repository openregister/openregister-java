package uk.gov.register.resources.v2;

import com.codahale.metrics.annotation.Timed;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.exceptions.NoSuchConfigException;
import uk.gov.register.resources.DataDeleteNotAllowed;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/")
public class DeleteRegisterDataResource {
    private RegisterContext registerContext;

    @Inject
    public DeleteRegisterDataResource(RegisterContext registerContext) {
        this.registerContext = registerContext;
    }

    @DELETE
    @PermitAll
    @Path("/delete-register-data")
    @DataDeleteNotAllowed
    @Timed
    public Response deleteRegisterData() throws NoSuchConfigException, IOException {
        registerContext.resetRegister();

        return Response.status(200).entity("Data has been deleted").build();
    }
}
