package uk.gov.register.views.representations.spreadsheet;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.EntryListView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
@Produces(ExtraMediaType.APPLICATION_SPREADSHEET)
public class EntriesSpreadSheetWriter extends RepresentationWriter<EntryListView> {

    @Override
    public void writeTo(final EntryListView entryListView, final Class<?> type, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
            throws IOException, WebApplicationException {
        final List<String> fieldNames = new ArrayList<>();
        final List<Map<String, String>> elements = new ArrayList<>();
        final Workbook workbook;

        fieldNames.add("index-entry-number");
        fieldNames.add("entry-number");
        fieldNames.add("entry-timestamp");
        fieldNames.add("key");
        fieldNames.add("item-hash");

        entryListView.getEntries().forEach(entry -> {
            final Map<String, String> element = new HashMap<>();
            String itemHashValues = StringUtils.EMPTY;

            element.put("index-entry-number", entry.getIndexEntryNumber().toString());
            element.put("entry-number", entry.getEntryNumber().toString());
            element.put("entry-timestamp", entry.getTimestampAsISOFormat());
            element.put("key", entry.getKey());

            if (!entry.getItemHashes().isEmpty()) {
                itemHashValues = entry.getItemHashes()
                        .stream().map(HashValue::toString).collect(Collectors.joining(";"));
            }

            element.put("item-hash", itemHashValues);

            elements.add(element);
        });

        workbook = WorkbookGenerator.toSpreadSheet("entries", fieldNames, elements);
        workbook.write(entityStream);
        entityStream.flush();
    }
}
