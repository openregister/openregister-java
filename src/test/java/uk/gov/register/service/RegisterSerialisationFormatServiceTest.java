package uk.gov.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.*;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.views.RegisterProof;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RegisterSerialisationFormatServiceTest {
    private CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

    @Mock
    private RegisterService registerService;

    @Mock
    private Register register;

    private Item item;
    private Entry entry1;
    private Entry entry2;
    private RegisterProof emptyRegisterProof;

    private RegisterCommand addItemCommand;
    private RegisterCommand appendEntryCommand1;
    private RegisterCommand appendEntryCommand2;
    private RegisterCommand assertRootEmptyRegister;


    private RegisterSerialisationFormatService sutService;

    @Before
    public void setUp() throws Exception {
        JsonNode content = new ObjectMapper().readTree("{\"address\":\"9AQZJ3M\",\"name\":\"ST LAWRENCE CHURCH\"}");

        item = new Item(content);
        entry1 = new Entry(1, getHash(content), Instant.now());
        entry2 = new Entry(2, getHash(content), Instant.now().plusMillis(100));
        emptyRegisterProof = new RegisterProof("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");

        addItemCommand = new AddItemCommand(item);
        appendEntryCommand1 = new AppendEntryCommand(entry1);
        appendEntryCommand2 = new AppendEntryCommand(entry2);
        assertRootEmptyRegister = new AssertRootHashCommand(emptyRegisterProof);


        sutService = new RegisterSerialisationFormatService(registerService, register);
    }

    @Test
    public void processRegisterComponents() throws Exception {
        when(register.getTotalEntries()).thenReturn(0);
        when(register.getRegisterProof()).thenReturn(emptyRegisterProof);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<Register> callback = (Consumer<Register>) invocation.getArguments()[0];
                callback.accept(register);
                return null;
            }
        }).when(registerService).asAtomicRegisterOperation(any(Consumer.class));

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(Arrays.asList(
                assertRootEmptyRegister,
                addItemCommand,
                appendEntryCommand1,
                appendEntryCommand2).iterator());

        InOrder inOrder = Mockito.inOrder(register);

        sutService.processRegisterComponents(rsf);

        verify(register, times(1)).getRegisterProof();
        verify(register, times(1)).putItem(item);
        verify(register, times(2)).appendEntry(any());

        inOrder.verify(register, calls(1)).appendEntry(entry1);
        inOrder.verify(register, calls(1)).appendEntry(entry2);
    }

    @Test
    public void createRegisterSerialisationFormat_returnsRSFFromEntireRegister() throws NoSuchAlgorithmException {
        when(register.getItemIterator()).thenReturn(Arrays.asList(item).iterator());
        when(register.getEntryIterator()).thenReturn(Arrays.asList(entry1, entry2).iterator());

        RegisterProof expectedRegisterProof = new RegisterProof("1231234");
        when(register.getRegisterProof()).thenReturn(expectedRegisterProof);

        RegisterSerialisationFormat actualRSF = sutService.createRegisterSerialisationFormat();
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        verify(register, times(1)).getItemIterator();
        verify(register, times(1)).getEntryIterator();
        verify(register, times(1)).getRegisterProof();

        AssertRootHashCommand expectedRootHashCommand = new AssertRootHashCommand(expectedRegisterProof);

        assertThat(actualCommands.size(), equalTo(5));
        assertThat(actualCommands, contains(
                assertRootEmptyRegister,
                addItemCommand,
                appendEntryCommand1,
                appendEntryCommand2,
                expectedRootHashCommand));
    }

    @Test
    public void createRegisterSerialisationFormat_whenCalledWithBoundary_returnsPartialRSFRegister() {
        RegisterProof oneEntryRegisterProof = new RegisterProof("oneEntryInRegisterHash");
        RegisterProof twoEntriesRegisterProof = new RegisterProof("twoEntriesInRegisterHash");
        RegisterCommand assertRootOneEntryInRegister = new AssertRootHashCommand(oneEntryRegisterProof);
        RegisterCommand assertRootTwoEntriesInRegister = new AssertRootHashCommand(twoEntriesRegisterProof);

        when(register.getItemIterator(2, 2)).thenReturn(Arrays.asList(item).iterator());
        when(register.getEntryIterator(2, 2)).thenReturn(Arrays.asList(entry2).iterator());
        when(register.getRegisterProof(1, 1)).thenReturn(oneEntryRegisterProof);
        when(register.getRegisterProof(2, 2)).thenReturn(twoEntriesRegisterProof);

        RegisterSerialisationFormat actualRSF = sutService.createRegisterSerialisationFormat(2, 2);
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        verify(register, times(1)).getItemIterator(2, 2);
        verify(register, times(1)).getEntryIterator(2, 2);

        assertThat(actualCommands.size(), equalTo(4));
        assertThat(actualCommands, contains(assertRootOneEntryInRegister, addItemCommand, appendEntryCommand2, assertRootTwoEntriesInRegister));
    }

    private String getHash(JsonNode content) {
        return DigestUtils.sha256Hex(canonicalJsonMapper.writeToBytes(content));
    }
}