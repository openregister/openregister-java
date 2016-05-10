package uk.gov.register.presentation.representations.turtle;

import io.dropwizard.views.View;
import org.apache.jena.rdf.model.Model;
import uk.gov.register.presentation.representations.RepresentationWriter;
import uk.gov.register.presentation.resource.RequestContext;

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
    protected static final String ITEM_FIELD_PREFIX = "//field.%s/record/";
    protected static final String ENTRY_PREFIX = "//%1$s.%2$s/entry/";
    protected static final String ITEM_PREFIX = "//%1$s.%2$s/item/";
    private static final String RECORD_PREFIX = "//%1$s.%2$s/record/";

    protected TurtleRepresentationWriter(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    public void writeTo(T view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        rdfModelFor(view).write(entityStream, "TURTLE");
    }

    protected abstract Model rdfModelFor(T view);

    protected URI entryUri(String entryNumber) {
        String path = String.format(ENTRY_PREFIX, requestContext.getRegisterPrimaryKey(), requestContext.getRegisterDomain());
        return uriWithScheme(path).path(entryNumber).build();
    }

    protected URI itemUri(String itemHash) {
        String path = String.format(ITEM_PREFIX, requestContext.getRegisterPrimaryKey(), requestContext.getRegisterDomain());
        return uriWithScheme(path).path(itemHash).build();
    }

    protected URI recordUri(String primaryKey) {
        String path = String.format(RECORD_PREFIX, requestContext.getRegisterPrimaryKey(), requestContext.getRegisterDomain());
        return uriWithScheme(path).path(primaryKey).build();
    }

    protected URI fieldUri() {
        String path = String.format(ITEM_FIELD_PREFIX, requestContext.getRegisterDomain());
        return uriWithScheme(path).build();
    }

    private UriBuilder uriWithScheme(String path) {
        return UriBuilder.fromPath(path).scheme(requestContext.getScheme());
    }
}
