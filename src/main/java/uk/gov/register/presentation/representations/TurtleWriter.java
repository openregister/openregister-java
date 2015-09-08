package uk.gov.register.presentation.representations;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.config.FieldConfiguration;
import uk.gov.register.presentation.config.FieldsConfiguration;

import javax.inject.Inject;
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
@Service
public class TurtleWriter extends RepresentationWriter {
    private static final String PREFIX = "@prefix field: <http://field.openregister.org/field/>.\n\n";

    private HttpServletRequest httpServletRequest;
    private FieldsConfiguration fieldsConfig;

    @Inject
    public TurtleWriter(@Context HttpServletRequest httpServletRequest, FieldsConfiguration fieldsConfig) {
        this.httpServletRequest = httpServletRequest;
        this.fieldsConfig = fieldsConfig;
    }

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
                .map(field -> renderField(record, field))
                .collect(Collectors.joining(" ;\n", entity, " ."));
    }

    private String renderField(Record record, String fieldName) {
        FieldConfiguration fieldConfig = fieldsConfig.getFields().get(fieldName);
        if (fieldConfig.getRegister().isPresent()) {
            return String.format(" field:%1$s <http://%2$s.openregister.org/%2$s/%3$s>", fieldName, fieldConfig.getRegister().get(), record.getEntry().get(fieldName));
        }
        return String.format(" field:%s \"%s\"", fieldName, record.getEntry().get(fieldName));
    }

    private URI uri(String hash) {
        return UriBuilder.fromUri(httpServletRequest.getRequestURL().toString()).replacePath(null).path("hash").path(hash).build();
    }
}
