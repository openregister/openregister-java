package uk.gov.register.presentation.representations;

import com.google.common.base.Throwables;
import uk.gov.register.presentation.Entry;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
import uk.gov.register.presentation.view.AbstractView;
import uk.gov.register.presentation.view.ListResultView;
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
public class TurtleWriter extends RepresentationWriter<AbstractView> {


    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(SingleResultView.class) || type.isAssignableFrom(ListResultView.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeTo(AbstractView view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        List<Entry> entries = getEntries(view.get());

        List<String> fields = entryFields(JsonObjectMapper.convert(entries.get(0).getContent(), Map.class));
        for (Entry entry : entries) {
            writeRow(entry, fields, entityStream);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Entry> getEntries(Object o) {
        if (o instanceof List) {
            return (List<Entry>) o;
        } else {
            List<Entry> entries = new ArrayList<>();
            entries.add((Entry) o);
            return entries;
        }
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