package uk.gov.register.presentation.representations;

import com.google.common.base.Throwables;
import uk.gov.register.presentation.Entry;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.view.SingleResultView;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Produces(ExtraMediaType.TEXT_TTL)
public class TurtleWriter extends RepresentationWriter<SingleResultView> {


    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(SingleResultView.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeTo(SingleResultView view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        Entry entry = view.getEntryObject();
        List<String> fields = entryFields(JsonObjectMapper.convert(entry.getContent(), Map.class));
        writeRow(entry, fields, entityStream);
    }


    //TODO: this should be retrieved from register call
    private static final String registerBaseUri = "http://localhost:9000";

    @SuppressWarnings("unchecked")
    private List entryFields(Map map) {
        return new ArrayList<>((map.keySet()));
    }

    public void writeRow(Entry entry, List<String> fields, OutputStream entityStream) {
        try {
            entityStream.write((renderRecord(entry, fields) + "\n").getBytes("utf-8"));
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    private String renderRecord(Entry node, List<String> fields) {
        URI hashUri = uri(node.getHash());
        String entity = String.format("<%s>;\n", hashUri);
        return fields.stream()
                .map(field -> String.format(" %s %s", field, node.getContent().get(field)))
                .collect(Collectors.joining(" ;\n", entity, " ."));
    }

    private URI uri(String hash) {
        return UriBuilder.fromPath(registerBaseUri + "/hash/" + hash).build();
    }
}