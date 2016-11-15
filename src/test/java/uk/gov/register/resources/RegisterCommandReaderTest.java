package uk.gov.register.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.serialization.CommandParser;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandList;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.util.CanonicalJsonValidator;
import uk.gov.register.util.ObjectReconstructor;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RegisterCommandReaderTest {
    private final Class<RegisterCommandList> type = RegisterCommandList.class;
    private RegisterCommandReader registerCommandReader;

    @Mock
    Type genericType;

    Annotation[] annotations = new Annotation[]{};

    @Mock
    MediaType mediaType;

    @Mock
    MultivaluedMap<String, String> httpHeaders;

    @Before
    public void setup() {
        ObjectReconstructor objectReconstructor = new ObjectReconstructor();
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
        CanonicalJsonValidator canonicalJsonValidator = new CanonicalJsonValidator();
        CommandParser commandParser = new CommandParser(objectReconstructor, canonicalJsonMapper, canonicalJsonValidator);
        registerCommandReader = new RegisterCommandReader(commandParser);
    }

    @Test
    public void shouldParseCommands() throws Exception {
        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register.tsv"))
        ) {
            RegisterCommandList registerCommandList = registerCommandReader.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
            List<RegisterCommand> commands = registerCommandList.commands;

            assertThat(commands.size(), is(4));
        }
    }

    @Test
    public void shouldParseCommandsEscaped() throws Exception {
        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register-escaped.tsv"))
        ) {
            RegisterCommandList registerCommandList = registerCommandReader.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
            List<RegisterCommand> commands = registerCommandList.commands;

            assertThat(commands.size(), is(2));
        }
    }
}