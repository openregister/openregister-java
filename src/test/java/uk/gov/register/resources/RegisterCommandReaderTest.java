package uk.gov.register.resources;

import com.google.common.collect.Iterators;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterSerialisationFormat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RegisterCommandReaderTest {

    Class<RegisterSerialisationFormat> type = RegisterSerialisationFormat.class;

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
        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register.tsv"))) {
            RegisterSerialisationFormat registerSerialisationFormat = parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
            RegisterCommand[] registerCommands = Iterators.toArray(registerSerialisationFormat.getCommands(), RegisterCommand.class);

            assertThat(registerCommands.length, is(4));
        }
    }

    @Test
    public void shouldParseCommandsEscaped() throws Exception {
        RegisterCommandReader parser = new RegisterCommandReader();
        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register-escaped.tsv"))) {
            RegisterSerialisationFormat registerSerialisationFormat = parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
            RegisterCommand[] registerCommands = Iterators.toArray(registerSerialisationFormat.getCommands(), RegisterCommand.class);

            assertThat(registerCommands.length, is(2));
        }
    }

}
