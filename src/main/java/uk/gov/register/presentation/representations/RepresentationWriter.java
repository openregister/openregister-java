package uk.gov.register.presentation.representations;

import io.dropwizard.views.View;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.resource.ResourceBase;

import javax.ws.rs.WebApplicationException;
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
    @Override
    public final long getSize(View t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // deprecated and ignored by Jersey 2. Returning -1 as per javadoc in the interface
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ResourceBase.SingleResultView.class.isAssignableFrom(type) || ResourceBase.ListResultView.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(View view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        if (view instanceof ResourceBase.SingleResultView) {
            writeRecordsTo(entityStream, Collections.singletonList(((ResourceBase.SingleResultView) view).getRecord()));
        }
        else {
            writeRecordsTo(entityStream, ((ResourceBase.ListResultView) view).getRecords());
        }
    }

    protected abstract void writeRecordsTo(OutputStream entityStream, List<Record> records) throws IOException, WebApplicationException;
}
