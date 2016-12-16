package uk.gov.register.resources;

import org.flywaydb.core.Flyway;
import uk.gov.register.configuration.ConfigManager;
import uk.gov.register.core.RegisterContext;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class DeleteRegisterDataResource {
    private Flyway flyway;
    private ConfigManager configManager;

    @Inject
    public DeleteRegisterDataResource(RegisterContext registerContext, ConfigManager configManager) {
        this.flyway = registerContext.getFlyway();
        this.configManager = configManager;
    }

    @DELETE
    @PermitAll
    @Path("/delete-register-data")
    @DataDeleteNotAllowed
    public Response deleteRegisterData() {
        flyway.clean();
        configManager.refreshConfig();
        flyway.migrate();

        return Response.status(200).entity("Data has been deleted").build();
    }
}
