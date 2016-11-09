package uk.gov.register.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.exceptions.SerializedRegisterParseException;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterComponents;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RegisterCommandReaderTest {

    Class<RegisterComponents> type = RegisterComponents.class;
    @Mock
    Type genericType;
    @Mock
    Annotation[] annotations;
    @Mock
    MediaType mediaType;
    @Mock
    MultivaluedMap<String, String> httpHeaders;

    @Test
    public void shouldParseCommands() throws Exception {
        RegisterCommandReader parser = new RegisterCommandReader();
        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register.tsv"))
        ) {
            RegisterComponents registerComponents = parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
            List<RegisterCommand> commands = registerComponents.commands;

            assertThat(commands.size(), is(4));


        }
    }

    @Test
    public void shouldParseCommandsEscaped() throws Exception {
        RegisterCommandReader parser = new RegisterCommandReader();
        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register-escaped.tsv"))
        ) {
            RegisterComponents registerComponents = parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
            List<RegisterCommand> commands = registerComponents.commands;

            assertThat(commands.size(), is(2));
        }
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenNoHash() throws Exception {
        RegisterCommandReader parser = new RegisterCommandReader();
        InputStream serializerRegisterStream = streamString("append-entry\t2016-10-12T17:45:19.757132");
        parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenNoContent() throws Exception {
        RegisterCommandReader parser = new RegisterCommandReader();
        InputStream serializerRegisterStream = streamString("add-item");
        parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenNoHashPrefix() throws Exception {
        RegisterCommandReader parser = new RegisterCommandReader();
        InputStream serializerRegisterStream = streamString("append-entry\t2016-10-12T17:45:19.757132\tabc123");
        parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldThrowExWhenInvalidCommand() throws Exception {
        RegisterCommandReader parser = new RegisterCommandReader();
        InputStream serializerRegisterStream = streamString("assert-root-hash\tabc123");
        parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
    }

    @Test(expected = DateTimeParseException.class)
    public void shouldFailIfTimestampNotIso() throws Exception {
        RegisterCommandReader parser = new RegisterCommandReader();
        InputStream serializerRegisterStream = streamString("append-entry\t20161212\tsha-256:abc123");
        parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
    }

    @Test(expected = SerializedRegisterParseException.class)
    public void shouldFailIfInvalidJson() throws Exception {
        RegisterCommandReader parser = new RegisterCommandReader();
        InputStream serializerRegisterStream = streamString("add-item\t{\"address\":\"9AQZJ3K\"");
        parser.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
    }


    private InputStream streamString(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }


}
