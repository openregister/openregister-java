package uk.gov.register.resources;

import uk.gov.register.serialization.CommandParser;
import uk.gov.register.serialization.RegisterSerialisationFormat;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces({ExtraMediaType.APPLICATION_RSF, ExtraMediaType.TEXT_HTML})
public class RegisterCommandWriter implements MessageBodyWriter<RegisterSerialisationFormat> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return RegisterSerialisationFormat.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(RegisterSerialisationFormat rsf, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(RegisterSerialisationFormat rsf, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        CommandParser commandParser = new CommandParser();
        httpHeaders.add("Content-Disposition", String.format("attachment; filename=rsf-%d.%s", System.currentTimeMillis(), commandParser.getFileExtension()));

        rsf.getCommands().forEachRemaining(command -> {
            try {
                entityStream.write(command.serialise(commandParser).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}