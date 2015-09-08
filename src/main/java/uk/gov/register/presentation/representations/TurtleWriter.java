package uk.gov.register.presentation.representations;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.config.Field;
import uk.gov.register.presentation.config.FieldsConfiguration;
import uk.gov.register.presentation.resource.RequestContext;

import javax.inject.Inject;
import javax.ws.rs.Produces;
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

    private final FieldsConfiguration fieldsConfig;

    private final RequestContext requestContext;

    @Inject
    public TurtleWriter(RequestContext  requestContext, FieldsConfiguration fieldsConfig) {
        this.requestContext = requestContext;
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
        Field field = fieldsConfig.getField(fieldName);
        Object fieldValue = record.getEntry().get(fieldName);
        return field.getLink(fieldValue).map(r -> formatLinkField(fieldName, r))
                .orElse(formatOrdinaryField(fieldName, fieldValue));
    }

    private String formatOrdinaryField(String fieldName, Object fieldValue) {
        return String.format(" field:%s \"%s\"", fieldName, fieldValue);
    }

    private String formatLinkField(String fieldName, String link) {
        return String.format(" field:%s <%s>", fieldName, link);
    }

    private URI uri(String hash) {
        return UriBuilder.fromUri(requestContext.requestUrl()).replacePath(null).path("hash").path(hash).build();
    }
}
