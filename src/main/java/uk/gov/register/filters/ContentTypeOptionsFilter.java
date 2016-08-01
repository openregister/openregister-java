package uk.gov.register.filters;

import com.google.common.net.HttpHeaders;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class ContentTypeOptionsFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().add(HttpHeaders.X_CONTENT_TYPE_OPTIONS, "nosniff");
    }
}
