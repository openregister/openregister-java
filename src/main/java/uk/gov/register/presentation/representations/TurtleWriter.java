package uk.gov.register.presentation.representations;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.LinkValue;
import uk.gov.register.presentation.config.Register;
import uk.gov.register.presentation.resource.RequestContext;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    protected void writeEntriesTo(OutputStream entityStream, Register register, List<EntryView> entries) throws IOException {
        Iterable<String> fields = entries.get(0).allFields();
        entityStream.write(PREFIX.getBytes("utf-8"));
        for (EntryView entry : entries) {
            entityStream.write((renderEntry(entry, fields) + "\n").getBytes("utf-8"));
        }
    }

    private String renderEntry(EntryView entry, Iterable<String> fields) {
        URI entryUri = uri(entry.getSerialNumber());
        String entity = String.format("<%s>\n", entryUri);
        return StreamSupport.stream(fields.spliterator(),false)
                .flatMap(field -> renderFieldIfPresent(field, entry.getField(field)))
                .collect(Collectors.joining(" ;\n", entity, " ."));
    }

    private Stream<String> renderFieldIfPresent(String fieldName, Optional<FieldValue> fieldO) {
        Optional<String> renderedField = fieldO.map(fieldValue -> renderField(fieldName, fieldValue));
        return optionalStream(renderedField);
    }

    private <T> Stream<T> optionalStream(Optional<T> optional) {
        return optional.map(Stream::of)
                .orElse(Stream.empty());
    }

    private String renderField(String fieldName, FieldValue value) {
        if (value.isLink()) {
            return String.format(" field:%s <%s>", fieldName, ((LinkValue) value).link());
        } else {
            return String.format(" field:%s \"%s\"", fieldName, value.value());
        }
    }

    private URI uri(int serialNumber) {
        return UriBuilder.fromUri(requestContext.requestUrl()).replacePath(null).path("entry").path(Integer.toString(serialNumber)).build();
    }
}
