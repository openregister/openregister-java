package uk.gov.register.views.representations.spreadsheet;

import org.apache.poi.ss.usermodel.Workbook;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Field;
import uk.gov.register.views.BlobView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RecordSpreadsheet {

    default void process(final Iterable<Field> fields, final Map<Entry, List<BlobView>> records, final String sheetLabel,
                         final OutputStream entityStream) throws IOException {
        final List<String> fieldNames = new ArrayList<>();
        final List<Map<String, String>> elements = new ArrayList<>();
        final Workbook workbook;

        fieldNames.add("index-entry-number");
        fieldNames.add("entry-number");
        fieldNames.add("entry-timestamp");
        fieldNames.add("key");

        fields.forEach(field -> fieldNames.add(field.fieldName));

        records.forEach((key, items) ->
            items.forEach(item -> {
                final Map<String, String> element = new HashMap<>();

                element.put("index-entry-number", key.getIndexEntryNumber().toString());
                element.put("entry-number", key.getEntryNumber().toString());
                element.put("entry-timestamp", key.getTimestampAsISOFormat());
                element.put("key", key.getKey());

                item.getContent().forEach((contentKey, contentValue) -> element.put(contentKey, contentValue.getValue()));

                elements.add(element);
            })
        );

        workbook = WorkbookGenerator.toSpreadSheet(sheetLabel, fieldNames, elements);
        workbook.write(entityStream);
        entityStream.flush();
    }
}
