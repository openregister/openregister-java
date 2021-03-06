package uk.gov.register.filters;

import uk.gov.register.configuration.DeleteRegisterDataConfiguration;
import uk.gov.register.resources.DataDeleteNotAllowed;
import uk.gov.register.views.ViewFactory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@DataDeleteNotAllowed
public class DeleteRegisterDataFilter implements ContainerRequestFilter {

    private final javax.inject.Provider<DeleteRegisterDataConfiguration> deleteRegisterDataConfiguration;
    private final ViewFactory viewFactory;

    @Inject
    public DeleteRegisterDataFilter(javax.inject.Provider<DeleteRegisterDataConfiguration> deleteRegisterDataConfiguration, ViewFactory viewFactory) {
        this.deleteRegisterDataConfiguration = deleteRegisterDataConfiguration;
        this.viewFactory = viewFactory;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!deleteRegisterDataConfiguration.get().getEnableRegisterDataDelete()) {
            Response response = Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(viewFactory.exceptionNotFoundView())
                    .build();

            requestContext.abortWith(response);
        }
    }
}
