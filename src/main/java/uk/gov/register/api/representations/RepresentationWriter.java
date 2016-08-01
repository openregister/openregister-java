package uk.gov.register.api.representations;

import uk.gov.register.resources.RequestContext;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public abstract class RepresentationWriter<T> implements MessageBodyWriter<T> {
    @Context
    protected RequestContext requestContext;

    @Override
    public final long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // deprecated and ignored by Jersey 2. Returning -1 as per javadoc in the interface
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }
}
