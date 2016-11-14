package uk.gov.register.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.register.serialization.CommandParser;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandList;
import uk.gov.register.views.representations.ExtraMediaType;

import javax.inject.Inject;
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

import static java.util.stream.Collectors.toList;

@Provider
@Consumes(ExtraMediaType.APPLICATION_RSF)
public class RegisterCommandReader implements MessageBodyReader<RegisterCommandList> {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterCommandReader.class);

    private final CommandParser commandParser;

    @Inject
    public RegisterCommandReader(CommandParser commandParser) {
        this.commandParser = commandParser;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == RegisterCommandList.class;
    }

    @Override
    public RegisterCommandList readFrom(Class<RegisterCommandList> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return parseCommands(entityStream);
    }

    private RegisterCommandList parseCommands(InputStream commandStream) {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(commandStream));
        List<RegisterCommand> commands = buffer.lines().map(s -> commandParser.newCommand(s)).collect(toList());
        // don't close the reader as the caller will close the input stream
        return new RegisterCommandList(commands);

    }
}

