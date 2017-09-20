package uk.gov.register.views.representations.spreadsheet;

import uk.gov.register.views.RecordsView;
import uk.gov.register.views.representations.ExtraMediaType;
import uk.gov.register.views.representations.RepresentationWriter;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces(ExtraMediaType.APPLICATION_SPREADSHEET)
public class RecordsSpreadSheetWriter extends RepresentationWriter<RecordsView> implements RecordSpreadsheet {

    @Override
    public void writeTo(final RecordsView recordsView, final Class<?> type, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        process(recordsView.getFields(), recordsView.getRecords(), "record", entityStream);
    }
}
