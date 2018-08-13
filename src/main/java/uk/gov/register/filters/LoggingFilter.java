package uk.gov.register.filters;

import org.slf4j.MDC;
import uk.gov.register.core.RegisterContext;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class LoggingFilter implements ContainerRequestFilter {
    private final javax.inject.Provider<RegisterContext> registerContext;

    @Inject
    public LoggingFilter(javax.inject.Provider<RegisterContext> registerContext) {
        this.registerContext = registerContext;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        MDC.put("register", registerContext.get().getRegisterId().value());
    }
}
