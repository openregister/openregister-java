package uk.gov.register.presentation.representations;

import org.jvnet.hk2.annotations.Service;
import uk.gov.register.presentation.EntryView;
import uk.gov.register.presentation.FieldValue;
import uk.gov.register.presentation.LinkValue;
import uk.gov.register.presentation.ListValue;
import uk.gov.register.presentation.config.RegisterDomainConfiguration;
import uk.gov.register.presentation.resource.RequestContext;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Produces(ExtraMediaType.TEXT_TTL)
@Service
public class TurtleWriter extends RepresentationWriter {
    private static final String PREFIX_FORMAT = "@prefix field: <http://field.%s/field/>.\n\n";

    private final RequestContext requestContext;
    private final String prefix;

    @Inject
    public TurtleWriter(RequestContext requestContext, RegisterDomainConfiguration registerDomainConfiguration) {
        this.requestContext = requestContext;
        this.prefix = String.format(PREFIX_FORMAT, registerDomainConfiguration.getRegisterDomain());
    }

    @Override
    protected void writeEntriesTo(OutputStream entityStream, Iterable<String> fields, List<EntryView> entries) throws IOException {
        entityStream.write(prefix.getBytes(StandardCharsets.UTF_8));
        for (EntryView entry : entries) {
            entityStream.write((renderEntry(entry, fields) + "\n").getBytes(StandardCharsets.UTF_8));
        }
    }

    private String renderEntry(EntryView entry, Iterable<String> fields) {
        URI entryUri = uri(entry.getSerialNumber());
        String entity = String.format("<%s>\n", entryUri);
        return StreamSupport.stream(fields.spliterator(),false)
                .flatMap(field -> new FieldRenderer(field).render(entry.getField(field)))
                .collect(Collectors.joining(" ;\n", entity, " ."));
    }


    private URI uri(int serialNumber) {
        return UriBuilder.fromUri(requestContext.requestUrl()).replacePath(null).path("entry").path(Integer.toString(serialNumber)).build();
    }

    private static class FieldRenderer {
        private final String fieldName;

        public FieldRenderer(String fieldName) {
            this.fieldName = fieldName;
        }

        public Stream<String> render(Optional<FieldValue> fieldO) {
            return optionalStream(fieldO).flatMap(this::renderField);
        }

        private Stream<String> renderField(FieldValue value) {
            if (value.isList()) {
                return renderList((ListValue) value);
            }
            else {
                return Stream.of(renderScalar(value));
            }
        }

        private Stream<String> renderList(ListValue listValue) {
            return listValue.stream().map(this::renderScalar);
        }

        private String renderScalar(FieldValue value) {
            if (value.isLink()) {
                return String.format(" field:%s <%s>", this.fieldName, ((LinkValue) value).link());
            } else {
                return String.format(" field:%s \"%s\"", this.fieldName, value.getValue());
            }
        }

        private <T> Stream<T> optionalStream(Optional<T> optional) {
            return optional.map(Stream::of)
                    .orElse(Stream.empty());
        }
    }
}
