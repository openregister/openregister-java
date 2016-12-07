package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import uk.gov.register.configuration.RegisterNameConfiguration;
import uk.gov.register.core.RegisterResolver;
import uk.gov.register.resources.RequestContext;
import uk.gov.register.views.representations.RepresentationWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;

public abstract class TurtleRepresentationWriter<T> extends RepresentationWriter<T> {
    protected static final String SPEC_PREFIX = "https://openregister.github.io/specification/#";
    protected RegisterResolver registerResolver;
    private String registerPrimaryKey;

    protected TurtleRepresentationWriter(RequestContext requestContext, RegisterNameConfiguration registerNameConfiguration, RegisterResolver registerResolver) {
        this.registerResolver = registerResolver;
        this.requestContext = requestContext;
        this.registerPrimaryKey = registerNameConfiguration.getRegisterName();
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
        return registerResolver.baseUriFor(registerPrimaryKey);
    }

    protected URI itemUri(String itemHash) {
        return UriBuilder.fromUri(ourBaseUri()).path("item").path(itemHash).build();
    }

}
