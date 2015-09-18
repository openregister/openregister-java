package uk.gov.register.presentation.representations;

import uk.gov.register.presentation.EntryView;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

@Produces(ExtraMediaType.TEXT_TSV)
public class TsvWriter extends RepresentationWriter {
    @Override
    protected void writeEntriesTo(OutputStream entityStream, List<EntryView> entries) throws IOException, WebApplicationException {
        List<String> fields = newArrayList(entries.get(0).allFields());
        entityStream.write(("entry\t" + String.join("\t", fields) + "\n").getBytes("utf-8"));
        for (EntryView entry : entries) {
            writeRow(entityStream, fields, entry);
        }
    }

    private void writeRow(OutputStream entityStream, List<String> fields, EntryView entry) throws IOException {
        String row = fields.stream().map(field -> entry.getField(field).value()).collect(Collectors.joining("\t", entry.getSerialNumber() + "\t", "\n"));
        entityStream.write(row.getBytes("utf-8"));
    }
}
