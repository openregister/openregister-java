package uk.gov.register.filters;

import io.pivotal.labs.cfenv.CloudFoundryEnvironment;
import io.pivotal.labs.cfenv.CloudFoundryEnvironmentException;
import uk.gov.register.configuration.DeleteRegisterDataConfiguration;
import uk.gov.register.resources.DataDeleteNotAllowed;
import uk.gov.register.resources.FutureAPI;
import uk.gov.register.views.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.NoSuchElementException;

@Provider
@FutureAPI
public class FutureAPIFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (isBetaEnvironment()) {
            Response response = Response
                    .status(Response.Status.NOT_IMPLEMENTED)
                    .build();

            requestContext.abortWith(response);
        }
    }

    private boolean isBetaEnvironment() {
        CloudFoundryEnvironment environment;
        String dbName;
        try {
            environment = new CloudFoundryEnvironment(System::getenv);
            dbName = environment.getService("postgres").getName();
        } catch(CloudFoundryEnvironmentException e) {
            // Not on cloud foundry
            return false;
        } catch (NoSuchElementException e) {
            // No database no problem
            return false;
        }

        return dbName.startsWith("beta-");
    }
}
