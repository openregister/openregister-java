package uk.gov.register.api.representations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces(ExtraMediaType.TEXT_YAML)
public class YamlWriter extends RepresentationWriter<Object> {
    private final ObjectMapper objectMapper;

    @Inject
    public YamlWriter() {
        objectMapper = Jackson.newObjectMapper(new YAMLFactory());
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        objectMapper.writeValue(entityStream, object);
    }
}
