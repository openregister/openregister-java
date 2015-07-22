package uk.gov.register.presentation.representations;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dropwizard.views.View;
import uk.gov.register.presentation.Entry;
import uk.gov.register.presentation.mapper.JsonObjectMapper;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Produces(ExtraMediaType.TEXT_TTL)
public class TurtleWriter extends RepresentationWriter<View> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return SingleResultView.class.isAssignableFrom(type) || ListResultView.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(View view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        List<Entry> entries =
                view instanceof SingleResultView ?
                        Collections.singletonList(((SingleResultView) view).getObject()) :
                        ((ListResultView) view).getObject();

        List<String> fields = entryFields(JsonObjectMapper.convert(entries.get(0).getContent(), new TypeReference<Map<String, Object>>() {
        }));
        for (Entry entry : entries) {
            entityStream.write((renderRecord(entry, fields) + "\n").getBytes("utf-8"));
        }
    }

    //TODO: this should be retrieved from register call
    private static final String registerBaseUri = "http://localhost:9000";

    private List<String> entryFields(Map<String, Object> map) {
        return new ArrayList<>((map.keySet()));
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