package uk.gov.register.presentation.representations;

import io.dropwizard.views.View;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.resource.RequestContext;
import uk.gov.register.presentation.view.EntryListView;
import uk.gov.register.presentation.view.SingleEntryView;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public abstract class RepresentationWriter implements MessageBodyWriter<View> {
    @Context
    private RequestContext requestContext;

    @Override
    public final long getSize(View t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // deprecated and ignored by Jersey 2. Returning -1 as per javadoc in the interface
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return SingleEntryView.class.isAssignableFrom(type) || EntryListView.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(View view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        if (view instanceof SingleEntryView) {
            writeEntryTo(entityStream, requestContext.getRegister().getFields(), ((SingleEntryView) view).getEntry());
        }
        else {
            writeEntriesTo(entityStream, requestContext.getRegister().getFields(), ((EntryListView) view).getEntries());
        }
    }

    protected void writeEntryTo(OutputStream entityStream, Iterable<String> fields, EntryView entry) throws IOException {
        writeEntriesTo(entityStream, fields, Collections.singletonList(entry));
    }

    protected abstract void writeEntriesTo(OutputStream entityStream, Iterable<String> fields, List<EntryView> entries) throws IOException, WebApplicationException;
}
