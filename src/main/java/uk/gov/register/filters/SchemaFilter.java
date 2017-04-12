package uk.gov.register.filters;

import uk.gov.register.core.RegisterContext;
import uk.gov.register.db.SchemaRewriter;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

@javax.ws.rs.ext.Provider
public class SchemaFilter implements ContainerRequestFilter {
    private final Provider<RegisterContext> registerContext;

    @Inject
    public SchemaFilter(Provider<RegisterContext> registerContext) {
        this.registerContext = registerContext;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        SchemaRewriter.schema.set(registerContext.get().getSchema());
    }
}
