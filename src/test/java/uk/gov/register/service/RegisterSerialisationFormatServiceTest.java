package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterators;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.IteratorUtils;
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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class RegisterSerialisationFormatServiceTest {

    private CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

    @Mock
    private RegisterService registerService;

    @Mock
    private Register register;

    private Iterator<RegisterCommand> commands;
    private Item item;
    private Entry entry1;
    private Entry entry2;
    private RegisterCommand addItemCommand;
    private RegisterCommand appendEntryCommand1;
    private RegisterCommand appendEntryCommand2;

    private RegisterSerialisationFormatService sutService;
    private RegisterSerialisationFormat rsf;

    @Before
    public void setUp() throws Exception {
        JsonNode content = new ObjectMapper().readTree("{\"address\":\"9AQZJ3M\",\"name\":\"ST LAWRENCE CHURCH\"}");

        item = new Item(content);
        entry1 = new Entry(0, getHash(content), Instant.now());
        entry2 = new Entry(1, getHash(content), Instant.now().plusMillis(100));

        addItemCommand = new AddItemCommand(item);
        appendEntryCommand1 = new AppendEntryCommand(entry1);
        appendEntryCommand2 = new AppendEntryCommand(entry2);

        commands = Arrays.asList(addItemCommand, appendEntryCommand1, appendEntryCommand2).iterator();

        rsf = new RegisterSerialisationFormat(commands);
        sutService = new RegisterSerialisationFormatService(registerService, register);
    }

    @Test
    public void processRegisterComponents() throws Exception {

        when(register.getTotalEntries()).thenReturn(0);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<Register> callback = (Consumer<Register>) invocation.getArguments()[0];
                callback.accept(register);
                return null;
            }
        }).when(registerService).asAtomicRegisterOperation(any(Consumer.class));

        sutService.processRegisterComponents(rsf);

        verify(register).putItem(item);
        verify(register).appendEntry(entry1);
    }

    @Test
    public void createRegisterSerialisationFormat_returnsRSFFromEntireRegister() {
        when(register.getItemIterator()).thenReturn(Arrays.asList(item).iterator());
        when(register.getEntryIterator()).thenReturn(Arrays.asList(entry1, entry2).iterator());

        RegisterSerialisationFormat actualRSF = sutService.createRegisterSerialisationFormat();
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        verify(register, times(1)).getItemIterator();
        verify(register, times(1)).getEntryIterator();

        assertThat(actualCommands.size(), equalTo(3));
        assertThat(actualCommands, contains(addItemCommand, appendEntryCommand1, appendEntryCommand2));
    }

    @Test
    public void createRegisterSerialisationFormat_whenCalledWithBoundary_returnsPartialRSFRegister() {
        when(register.getItemIterator(2, 2)).thenReturn(Arrays.asList(item).iterator());
        when(register.getEntryIterator(2, 2)).thenReturn(Arrays.asList(entry2).iterator());

        RegisterSerialisationFormat actualRSF = sutService.createRegisterSerialisationFormat(2, 2);
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        verify(register, times(1)).getItemIterator(2, 2);
        verify(register, times(1)).getEntryIterator(2, 2);


        assertThat(actualCommands.size(), equalTo(2));
        assertThat(actualCommands, contains(addItemCommand, appendEntryCommand2));
    }


    private String getHash(JsonNode content) {
        return DigestUtils.sha256Hex(canonicalJsonMapper.writeToBytes(content));
    }


}