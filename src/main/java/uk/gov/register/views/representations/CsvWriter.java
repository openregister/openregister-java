package uk.gov.register.views.representations;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import uk.gov.register.views.CsvRepresentationView;

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
@Produces(ExtraMediaType.TEXT_CSV)
public class CsvWriter extends RepresentationWriter<CsvRepresentationView> {
    private final CsvMapper objectMapper;

    public CsvWriter() {
        objectMapper = new CsvMapper();
    }

    @Override
    public void writeTo(CsvRepresentationView view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        CsvRepresentation csvRepresentation = view.csvRepresentation();

        objectMapper.writerFor(csvRepresentation.contentType)
                .with(csvRepresentation.csvSchema.withLineSeparator("\r\n").withHeader())
                .writeValue(entityStream, csvRepresentation.contents);
    }
}
