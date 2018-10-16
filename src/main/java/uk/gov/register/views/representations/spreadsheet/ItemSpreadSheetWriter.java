package uk.gov.register.views.representations.spreadsheet;

import org.apache.poi.ss.usermodel.Workbook;
import uk.gov.register.views.BlobView;
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
import java.util.*;

@Provider
@Produces(ExtraMediaType.APPLICATION_SPREADSHEET)
public class ItemSpreadSheetWriter extends RepresentationWriter<BlobView> {

    @Override
    public void writeTo(final BlobView blobView, final Class<?> type, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        final List<String> fieldNames = new ArrayList<>();
        final Map<String, String> element = new HashMap<>();
        final Workbook workbook;

        blobView.getFields().forEach(field -> fieldNames.add(field.fieldName));

        blobView.getContent().forEach((fieldName, fieldValue) -> element.put(fieldName, fieldValue.getValue()));

        workbook = WorkbookGenerator.toSpreadSheet("item", fieldNames, Collections.singletonList(element));
        workbook.write(entityStream);
        entityStream.flush();
    }
}
