//package uk.gov.register.resources;
//
//import com.google.common.collect.Iterators;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//import uk.gov.register.serialization.RegisterCommand;
//import uk.gov.register.serialization.RegisterSerialisationFormat;
//
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.MultivaluedMap;
//import java.io.InputStream;
//import java.lang.annotation.Annotation;
//import java.lang.reflect.Type;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//
//import static org.hamcrest.core.Is.is;
//import static org.junit.Assert.assertThat;
//
//@RunWith(MockitoJUnitRunner.class)
//public class RegisterCommandReaderTest {
//
//    private final Class<RegisterSerialisationFormat> type = RegisterSerialisationFormat.class;
//
//    @Mock
//    private Type genericType;
//
//    private Annotation[] annotations = new Annotation[]{};
//
//    @Mock
//    private MediaType mediaType;
//
//    @Mock
//    private MultivaluedMap<String, String> httpHeaders;
//
//    private RegisterCommandReader registerCommandReader;
//
//    @Before
//    public void setup() {
//        registerCommandReader = new RegisterCommandReader();
//    }
//
//    @Test
//    public void shouldParseCommands() throws Exception {
//        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "register-register-rsf.tsv"))) {
//            RegisterSerialisationFormat registerSerialisationFormat = registerCommandReader.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
//            RegisterCommand[] registerCommands = Iterators.toArray(registerSerialisationFormat.getCommands(), RegisterCommand.class);
//
//            assertThat(registerCommands.length, is(7));
//        }
//    }
//
//    @Test
//    public void shouldParseCommandsEscaped() throws Exception {
//        try (InputStream serializerRegisterStream = Files.newInputStream(Paths.get("src/test/resources/fixtures/serialized", "valid-register-escaped.tsv"))) {
//            RegisterSerialisationFormat registerSerialisationFormat = registerCommandReader.readFrom(type, genericType, annotations, mediaType, httpHeaders, serializerRegisterStream);
//            RegisterCommand[] registerCommands = Iterators.toArray(registerSerialisationFormat.getCommands(), RegisterCommand.class);
//
//            assertThat(registerCommands.length, is(2));
//        }
//    }
//}
