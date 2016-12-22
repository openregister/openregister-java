package uk.gov.register.auth;

import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthDynamicFeature;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.lang.annotation.Annotation;

/**
 * This is a copypaste of {@link AuthDynamicFeature}, except that it registers
 * a Filter by class instead of by instance.  This means that the filter can have
 * dependencies injected into it at runtime.
 */
public class RegisterAuthDynamicFeature implements DynamicFeature {

    private final Class<? extends ContainerRequestFilter> filterClass;

    public RegisterAuthDynamicFeature(Class<? extends ContainerRequestFilter> filterClass) {
        this.filterClass = filterClass;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
        final Annotation[][] parameterAnnotations = am.getParameterAnnotations();
        //@DenyAll shouldn't be attached to classes
        final boolean annotationOnClass = (resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class) != null) ||
                (resourceInfo.getResourceClass().getAnnotation(PermitAll.class) != null);
        final boolean annotationOnMethod = am.isAnnotationPresent(RolesAllowed.class) || am.isAnnotationPresent(DenyAll.class) ||
                am.isAnnotationPresent(PermitAll.class);

        if (annotationOnClass || annotationOnMethod) {
            context.register(filterClass);
        } else {
            for (Annotation[] annotations : parameterAnnotations) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Auth) {
                        context.register(filterClass);
                        return;
                    }
                }
            }
        }
    }
}
