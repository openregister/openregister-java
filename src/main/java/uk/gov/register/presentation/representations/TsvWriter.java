package uk.gov.register.presentation.representations;

import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.ListValue;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Provider
@Produces(ExtraMediaType.TEXT_TSV)
public class TsvWriter extends RepresentationWriter {
    @Override
    protected void writeEntriesTo(OutputStream entityStream, Iterable<String> fields, List<EntryView> entries) throws IOException, WebApplicationException {
        entityStream.write(("entry\t" + String.join("\t", fields) + "\n").getBytes(StandardCharsets.UTF_8));
        for (EntryView entry : entries) {
            writeRow(entityStream, fields, entry);
        }
    }

    private void writeRow(OutputStream entityStream, Iterable<String> fields, EntryView entry) throws IOException {
        String row = StreamSupport.stream(fields.spliterator(),false)
                .map(field -> entry.getField(field).map(this::renderField).orElse(""))
                .collect(Collectors.joining("\t", entry.getSerialNumber() + "\t", "\n"));
        entityStream.write(row.getBytes(StandardCharsets.UTF_8));
    }

    private String renderField(FieldValue fieldValue) {
        if (fieldValue.isList()) {
            return renderList((ListValue) fieldValue);
        }
        return escape(fieldValue.getValue());
    }

    private String escape(String value) {
        return value.replace("\n","\\n");
    }

    private String renderList(ListValue listValue) {
        return listValue.stream()
                .map(FieldValue::getValue)
                .collect(Collectors.joining(";"));
    }
}
