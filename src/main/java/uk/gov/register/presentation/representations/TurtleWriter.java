package uk.gov.register.presentation.representations;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.LinkValue;
import uk.gov.register.presentation.EntryView;
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

    private final RequestContext requestContext;

    @Inject
    public TurtleWriter(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    protected void writeEntriesTo(OutputStream entityStream, List<EntryView> entries) throws IOException {
        Set<String> fields = entries.get(0).allFields();
        entityStream.write(PREFIX.getBytes("utf-8"));
        for (EntryView entry : entries) {
            entityStream.write((renderEntry(entry, fields) + "\n").getBytes("utf-8"));
        }
    }

    private String renderEntry(EntryView entry, Set<String> fields) {
        URI hashUri = uri(entry.getHash());
        String entity = String.format("<%s>\n", hashUri);
        return fields.stream()
                .map(field -> renderField(entry, field))
                .collect(Collectors.joining(" ;\n", entity, " ."));
    }

    private String renderField(EntryView entry, String fieldName) {
        FieldValue value = entry.getField(fieldName);
        if (value.isLink()) {
            return String.format(" field:%s <%s>", fieldName, ((LinkValue)value).link());
        } else {
            return String.format(" field:%s \"%s\"", fieldName, value.value());
        }
    }

    private URI uri(String hash) {
        return UriBuilder.fromUri(requestContext.requestUrl()).replacePath(null).path("hash").path(hash).build();
    }
}
