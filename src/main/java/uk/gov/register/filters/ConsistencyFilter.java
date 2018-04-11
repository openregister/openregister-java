package uk.gov.register.filters;

import uk.gov.register.core.RegisterContext;
import uk.gov.register.core.RegisterId;
import uk.gov.register.exceptions.InconsistencyException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class ConsistencyFilter implements ContainerRequestFilter {

    private final javax.inject.Provider<RegisterId> registerPrimaryKey;
    private final UriInfo uriInfo;
    private final javax.inject.Provider<RegisterContext> registerContext;

    @Inject
    public ConsistencyFilter(final javax.inject.Provider<RegisterId> registerPrimaryKey,
                             final UriInfo uriInfo,
                             javax.inject.Provider<RegisterContext> registerContext) {
        this.registerPrimaryKey = registerPrimaryKey;
        this.uriInfo = uriInfo;
        this.registerContext = registerContext;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final String path = uriInfo.getPath();

        if (skipFilter(path)) {
            return;
        }

        if (!registerContext.get().hasConsistentState()) {
            throw new InconsistencyException(
                    String.format("Register %s doesn't match with specification.", registerPrimaryKey.get().value()));
        }
    }

    private boolean skipFilter(final String path) {
        return path.contains("delete-register-data") || path.contains("load-rsf");
    }
}
