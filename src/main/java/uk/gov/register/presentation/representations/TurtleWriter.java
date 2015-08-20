package uk.gov.register.presentation.representations;

import io.dropwizard.views.View;
import uk.gov.register.presentation.Record;
import uk.gov.register.presentation.resource.ResourceBase;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Produces(ExtraMediaType.TEXT_TTL)
public class TurtleWriter extends RepresentationWriter<View> {
    private static final String PREFIX = "@prefix field: <http://field.openregister.org/field/>.\n\n";

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ResourceBase.SingleResultView.class.isAssignableFrom(type) || ResourceBase.ListResultView.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(View view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        List<Record> records =
                view instanceof ResourceBase.SingleResultView ?
                        Collections.singletonList(((ResourceBase.SingleResultView) view).getRecord()) :
                        ((ResourceBase.ListResultView) view).getRecords();

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
