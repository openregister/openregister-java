package uk.gov.register.presentation.representations;

import uk.gov.register.presentation.RecordView;

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
    protected void writeRecordsTo(OutputStream entityStream, List<RecordView> records) throws IOException, WebApplicationException {
        List<String> fields = newArrayList(records.get(0).allFields());
        entityStream.write(("hash\t" + String.join("\t", fields) + "\n").getBytes("utf-8"));
        for (RecordView record : records) {
            writeRow(entityStream, fields, record);
        }
    }

    private void writeRow(OutputStream entityStream, List<String> fields, RecordView record) throws IOException {
        String row = fields.stream().map(field -> record.getField(field).value()).collect(Collectors.joining("\t", record.getHash() + "\t", "\n"));
        entityStream.write(row.getBytes("utf-8"));
    }
}
