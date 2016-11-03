package uk.gov.register.resources;

import uk.gov.register.serialization.RegisterComponents;
import uk.gov.register.util.SerializedRegisterParser;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes("application/uk-gov-rsf")
public class RegisterComponentReader implements MessageBodyReader<RegisterComponents> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == RegisterComponents.class;
    }

    @Override
    public RegisterComponents readFrom(Class<RegisterComponents> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return new SerializedRegisterParser().parseCommands(entityStream);
    }
}
