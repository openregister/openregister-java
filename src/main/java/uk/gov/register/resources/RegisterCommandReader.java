package uk.gov.register.resources;

import uk.gov.register.serialization.CommandParser;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterSerialisationFormat;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

@Provider
@Consumes(ExtraMediaType.APPLICATION_RSF)
public class RegisterCommandReader implements MessageBodyReader<RegisterSerialisationFormat> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == RegisterSerialisationFormat.class;
    }

    @Override
    public RegisterSerialisationFormat readFrom(Class<RegisterSerialisationFormat> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return parseCommands(entityStream);
    }

    private RegisterSerialisationFormat parseCommands(InputStream commandStream) {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(commandStream));
        final CommandParser parser = new CommandParser();
        buffer.lines().forEach(parser::addCommand);
        Iterator<RegisterCommand> commands = parser.getCommands();
        // don't close the reader as the caller will close the input stream
        return new RegisterSerialisationFormat(commands);

    }
}

