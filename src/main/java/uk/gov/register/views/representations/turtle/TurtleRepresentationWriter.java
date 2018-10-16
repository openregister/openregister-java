package uk.gov.register.views.representations.turtle;

import org.apache.jena.rdf.model.Model;
import uk.gov.register.core.RegisterId;
import uk.gov.register.core.RegisterResolver;
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

public abstract class TurtleRepresentationWriter<T> extends RepresentationWriter<T> {
    protected static final String SPEC_PREFIX = "https://openregister.github.io/specification/#";
    protected final RegisterResolver registerResolver;
    protected final Provider<RegisterId> registerIdProvider;

    protected TurtleRepresentationWriter(javax.inject.Provider<RegisterId> registerIdProvider, RegisterResolver registerResolver) {
        this.registerIdProvider = registerIdProvider;
        this.registerResolver = registerResolver;
    }

    @Override
    public void writeTo(T view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        rdfModelFor(view).write(entityStream, "TURTLE");
    }

    protected abstract Model rdfModelFor(T view);

    protected URI entryUri(String entryNumber) {
        return UriBuilder.fromUri(ourBaseUri()).path("entries").path(entryNumber).build();
    }

    protected URI ourBaseUri() {
        return registerResolver.baseUriFor(registerIdProvider.get());
    }

    protected URI blobUri(String itemHash) {
        return UriBuilder.fromUri(ourBaseUri()).path("items").path(itemHash).build();
    }

}
