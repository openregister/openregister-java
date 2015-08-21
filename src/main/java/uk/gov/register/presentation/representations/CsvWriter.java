package uk.gov.register.presentation.representations;

import org.apache.commons.lang3.StringEscapeUtils;
import uk.gov.register.presentation.Record;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Produces(ExtraMediaType.TEXT_CSV)
public class CsvWriter extends RepresentationWriter {
    @Override
    protected void writeRecordsTo(OutputStream entityStream, List<Record> records) throws IOException, WebApplicationException {
        List<String> headers = getHeaders(records.get(0));
        entityStream.write((String.join(",", headers) + "\r\n").getBytes("utf-8"));
        for (Record record : records) {
            writeRow(entityStream, headers, record);
        }
    }

    private void writeRow(OutputStream entityStream, List<String> headers, Record record) throws IOException {
        Map<String, Object> entry = record.getEntry();
        entry.put("hash", record.getHash());
        String row = headers.stream().map(e -> escape(entry.get(e).toString())).collect(Collectors.joining(",", "", "\r\n"));
        entityStream.write(row.getBytes("utf-8"));
    }

    private String escape(String data) {
        return StringEscapeUtils.escapeCsv(data);
    }

    private List<String> getHeaders(Record record) {
        List<String> headers = new ArrayList<>();
        headers.add("hash");
        headers.addAll(record.getEntry().keySet());
        return headers;
    }
}
