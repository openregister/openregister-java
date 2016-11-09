package uk.gov.register.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.serialization.CommandParser;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterComponents;

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
import java.util.List;
import java.util.stream.Collectors;

@Provider
@Consumes("application/uk-gov-rsf")
public class RegisterCommandReader implements MessageBodyReader<RegisterComponents> {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterCommandReader.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == RegisterComponents.class;
    }

    @Override
    public RegisterComponents readFrom(Class<RegisterComponents> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return parseCommands(entityStream);
    }

    private RegisterComponents parseCommands(InputStream commandStream) {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(commandStream));

        List<RegisterCommand> commands = buffer.lines().map(s -> new CommandParser().newCommand(s)).collect(Collectors.toList());
        // don't close the reader as the caller will close the input stream
        return new RegisterComponents(commands);

    }
}
