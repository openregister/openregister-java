package uk.gov.register.presentation.filter;

import com.google.common.net.HttpHeaders;
import io.dropwizard.jersey.caching.CacheControl;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Filter to provide a default Cache-control: header of "no-transform".  Can be overridden with a {@link CacheControl}
 * annotation on the resource method.
 */
@Provider
public class CacheNoTransformFilterFactory implements DynamicFeature {
    private static final NoTransformFilter NO_TRANSFORM_FILTER = new NoTransformFilter();
    public static final String NO_TRANSFORM = "no-transform";

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        /* don't add a header if the method provides its own annotation */
        CacheControl cacheControl = resourceInfo.getResourceMethod().getAnnotation(CacheControl.class);
        if (cacheControl == null) {
            context.register(NO_TRANSFORM_FILTER);
        }
    }

    private static class NoTransformFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, NO_TRANSFORM);
        }
    }
}
