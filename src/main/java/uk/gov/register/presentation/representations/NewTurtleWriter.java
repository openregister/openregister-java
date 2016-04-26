package uk.gov.register.presentation.representations;

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
@Produces(ExtraMediaType.TEXT_TTL)
public class NewTurtleWriter extends NewRepresentationWriter {
    
    public NewTurtleWriter() {
    }

    @Override
    public void writeTo(RepresentationView view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        view.turtleRepresentation().write(entityStream, "TURTLE");
    }
}