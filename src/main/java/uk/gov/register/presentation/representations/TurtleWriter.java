package uk.gov.register.presentation.representations;

import uk.gov.register.presentation.Record;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Produces(ExtraMediaType.TEXT_TTL)
public class TurtleWriter extends RepresentationWriter {
    private static final String PREFIX = "@prefix field: <http://field.openregister.org/field/>.\n\n";

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    protected void writeRecordsTo(OutputStream entityStream, List<Record> records) throws IOException {
        Set<String> fields = records.get(0).getEntry().keySet();
        entityStream.write(PREFIX.getBytes("utf-8"));
        for (Record record : records) {
            entityStream.write((renderRecord(record, fields) + "\n").getBytes("utf-8"));
        }
    }

    private String renderRecord(Record record, Set<String> fields) {
        URI hashUri = uri(record.getHash());
        String entity = String.format("<%s>\n", hashUri);
        return fields.stream()
                .map(field -> String.format(" field:%s \"%s\"", field, record.getEntry().get(field)))
                .collect(Collectors.joining(" ;\n", entity, " ."));
    }

    private URI uri(String hash) {
        return UriBuilder.fromUri(httpServletRequest.getRequestURL().toString()).replacePath(null).path("hash").path(hash).build();
    }
}
