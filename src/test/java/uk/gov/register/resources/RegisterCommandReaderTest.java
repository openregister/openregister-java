package uk.gov.register.resources;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandList;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RegisterCommandReaderTest {

    Class<RegisterCommandList> type = RegisterCommandList.class;

    @Mock
    Type genericType;

    Annotation[] annotations = new Annotation[]{};

    @Mock
    MediaType mediaType;

    @Mock
    MultivaluedMap<String, String> httpHeaders;

    @Test
    public void shouldParseCommands() throws Exception {
        RegisterCommandReader parser = new RegisterCommandReader();
        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register.tsv"))
        ) {
            RegisterCommandList registerCommandList = parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
            List<RegisterCommand> commands = registerCommandList.commands;

            assertThat(commands.size(), is(4));
        }
    }

    @Test
    public void shouldParseCommandsEscaped() throws Exception {
        RegisterCommandReader parser = new RegisterCommandReader();
        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register-escaped.tsv"))
        ) {
            RegisterCommandList registerCommandList = parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
            List<RegisterCommand> commands = registerCommandList.commands;

            assertThat(commands.size(), is(2));
        }
    }

}
