package uk.gov.register.views.representations.turtle;

import io.dropwizard.views.View;
import org.apache.jena.rdf.model.Model;
import uk.gov.register.core.RegisterData;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.representations.RepresentationWriter;

import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;

public abstract class TurtleRepresentationWriter<T extends View> extends RepresentationWriter<T> {
    protected static final String SPEC_PREFIX = "https://openregister.github.io/specification/#";
    private final Provider<RegisterData> register;
    protected RegisterResolver registerResolver;

    protected TurtleRepresentationWriter(RequestContext requestContext, Provider<RegisterData> register, RegisterResolver registerResolver) {
        this.register = register;
        this.registerResolver = registerResolver;
        this.requestContext = requestContext;
    }

    @Override
    public void writeTo(T view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        rdfModelFor(view).write(entityStream, "TURTLE");
    }

    protected abstract Model rdfModelFor(T view);

    protected URI entryUri(String entryNumber) {
        return UriBuilder.fromUri(ourBaseUri()).path("entry").path(entryNumber).build();
    }

    protected URI ourBaseUri() {
        return registerResolver.baseUriFor(register.get().getRegister().getRegisterName());
    }

    protected URI itemUri(String itemHash) {
        return UriBuilder.fromUri(ourBaseUri()).path("item").path(itemHash).build();
    }

}
