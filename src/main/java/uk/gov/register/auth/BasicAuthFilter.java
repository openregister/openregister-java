package uk.gov.register.auth;

import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import uk.gov.register.core.RegisterContext;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

public class BasicAuthFilter implements ContainerRequestFilter {
    private final Provider<RegisterContext> registerContextProvider;

    @Inject
    public BasicAuthFilter(Provider<RegisterContext> registerContextProvider) {
        this.registerContextProvider = registerContextProvider;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        RegisterContext registerContext = registerContextProvider.get();
        BasicCredentialAuthFilter<RegisterAuthenticator.User> delegateFilter =
                new BasicCredentialAuthFilter.Builder<RegisterAuthenticator.User>()
                .setAuthenticator(registerContext.getAuthenticator())
                .buildAuthFilter();
        delegateFilter.filter(requestContext);
    }
}
