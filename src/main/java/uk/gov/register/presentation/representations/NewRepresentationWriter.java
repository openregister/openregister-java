package uk.gov.register.presentation.representations;

import io.dropwizard.views.View;
import uk.gov.register.presentation.dao.Entry;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.presentation.view.NewEntryView;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public abstract class NewRepresentationWriter implements MessageBodyWriter<View> {
    @Context
    private RequestContext requestContext;

    @Override
    public final long getSize(View t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // deprecated and ignored by Jersey 2. Returning -1 as per javadoc in the interface
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return NewEntryView.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(View view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        if (view instanceof NewEntryView) {
            writeEntryTo(entityStream, requestContext.getRegister().getFields(), ((NewEntryView) view).getEntry());
        }
    }

    protected abstract void writeEntryTo(OutputStream entityStream, Iterable<String> fields, Entry entry) throws IOException, WebApplicationException;
}
