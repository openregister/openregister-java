package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.AddItemCommand;
import uk.gov.register.serialization.AppendEntryCommand;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterSerialisationFormat;
import uk.gov.register.util.CanonicalJsonMapper;

import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegisterSerialisationFormatServiceTest {

    private CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

    @Mock
    private RegisterService registerService;

    @Mock
    private Register register;

    private Iterator<RegisterCommand> commands;
    private Item item;
    private Entry entry;
    private RegisterSerialisationFormatService service;
    private RegisterSerialisationFormat rsf;

    @Before
    public void setUp() throws Exception {
        JsonNode content = new ObjectMapper().readTree("{\"address\":\"9AQZJ3M\",\"name\":\"ST LAWRENCE CHURCH\"}");

        item = new Item(content);
        AddItemCommand itemCommand = new AddItemCommand(item);

        entry = new Entry(0, getHash(content), Instant.now());
        AppendEntryCommand entryCommand = new AppendEntryCommand(entry);

        commands = Arrays.asList(itemCommand, entryCommand).iterator();
        rsf = new RegisterSerialisationFormat(commands);

        service = new RegisterSerialisationFormatService(registerService, register);
    }

    @Test
    public void processRegisterComponents() throws Exception {

        when(register.getTotalEntries()).thenReturn(0);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<Register> callback = (Consumer<Register>)invocation.getArguments()[0];
                callback.accept(register);
                return null;
            }
        }).when(registerService).asAtomicRegisterOperation(any(Consumer.class));

        service.processRegisterComponents(rsf);

        verify(register).putItem(item);
        verify(register).appendEntry(entry);


    }

    private String getHash(JsonNode content) {
        return DigestUtils.sha256Hex(canonicalJsonMapper.writeToBytes(content));
    }


}