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
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.core.RegisterContext;
import uk.gov.register.serialization.*;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RegisterSerialisationFormatServiceTest {
    private CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

    @Mock
    private RegisterContext registerContext;

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
        entry1 = new Entry(1, getHash(content), Instant.now(), "9AQZJ3M");
        entry2 = new Entry(2, getHash(content), Instant.now().plusMillis(100), "9AQZJ3M");
        emptyRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));

        addItemCommand = new AddItemCommand(item);
        appendEntryCommand1 = new AppendEntryCommand(entry1);
        appendEntryCommand2 = new AppendEntryCommand(entry2);
        assertRootEmptyRegister = new AssertRootHashCommand(emptyRegisterProof);

        when(registerContext.buildOnDemandRegister()).thenReturn(register);
        sutService = new RegisterSerialisationFormatService(registerContext);
    }

    @Test
    public void processRegisterComponents() throws Exception {
        when(register.getTotalEntries()).thenReturn(0).thenReturn(1);
        when(register.getRegisterProof()).thenReturn(emptyRegisterProof);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<Register> callback = (Consumer<Register>) invocation.getArguments()[0];
                callback.accept(register);
                return null;
            }
        }).when(registerContext).transactionalRegisterOperation(any(Consumer.class));

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

        inOrder.verify(register, calls(1)).appendEntry(new Entry(1, entry1.getSha256hex(), entry1.getTimestamp(), entry1.getKey()));
        inOrder.verify(register, calls(1)).appendEntry(new Entry(2, entry2.getSha256hex(), entry2.getTimestamp(), entry2.getKey()));
    }

    @Test
    public void createRegisterSerialisationFormat_returnsRSFFromEntireRegister() throws NoSuchAlgorithmException {
        when(register.getItemIterator()).thenReturn(singletonList(item).iterator());
        when(register.getEntryIterator()).thenReturn(Arrays.asList(entry1, entry2).iterator());

        RegisterProof expectedRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "1231234"));
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
        RegisterProof oneEntryRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "oneEntryInRegisterHash"));
        RegisterProof twoEntriesRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "twoEntriesInRegisterHash"));
        RegisterCommand assertRootOneEntryInRegister = new AssertRootHashCommand(oneEntryRegisterProof);
        RegisterCommand assertRootTwoEntriesInRegister = new AssertRootHashCommand(twoEntriesRegisterProof);

        when(register.getItemIterator(1, 2)).thenReturn(singletonList(item).iterator());
        when(register.getEntryIterator(1, 2)).thenReturn(singletonList(entry2).iterator());
        when(register.getRegisterProof(1)).thenReturn(oneEntryRegisterProof);
        when(register.getRegisterProof(2)).thenReturn(twoEntriesRegisterProof);

        RegisterSerialisationFormat actualRSF = sutService.createRegisterSerialisationFormat(1, 2);
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        verify(register, times(1)).getItemIterator(1, 2);
        verify(register, times(1)).getEntryIterator(1, 2);

        assertThat(actualCommands.size(), equalTo(4));
        assertThat(actualCommands, contains(assertRootOneEntryInRegister, addItemCommand, appendEntryCommand2, assertRootTwoEntriesInRegister));
    }

    private HashValue getHash(JsonNode content) {
        String hash = DigestUtils.sha256Hex(canonicalJsonMapper.writeToBytes(content));

        return new HashValue(HashingAlgorithm.SHA256, hash);
    }
}
