package uk.gov.register.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.serialization.RegisterSerialisationFormat;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

//@Provider
//@Consumes(ExtraMediaType.APPLICATION_RSF)
public class RegisterCommandReader implements MessageBodyReader<RegisterSerialisationFormat> {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterCommandReader.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == RegisterSerialisationFormat.class;
    }

    @Override
    public RegisterSerialisationFormat readFrom(Class<RegisterSerialisationFormat> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return null;
    }


}

