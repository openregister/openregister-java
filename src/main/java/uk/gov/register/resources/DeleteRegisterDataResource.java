package uk.gov.register.resources;

import org.flywaydb.core.Flyway;
import uk.gov.register.core.RegisterContext;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class DeleteRegisterDataResource {
    private Flyway flyway;

    @Inject
    public DeleteRegisterDataResource(RegisterContext registerContext) {
        this.flyway = registerContext.getFlyway();
    }

    @DELETE
    @PermitAll
    @Path("/delete-register-data")
    @DataDeleteNotAllowed
    public Response deleteRegisterData() {
        flyway.clean();
        flyway.migrate();
        return Response.status(200).entity("Data has been deleted").build();
    }
}
