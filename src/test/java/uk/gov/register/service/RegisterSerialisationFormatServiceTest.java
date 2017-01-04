package uk.gov.register.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Iterators;
import org.apache.commons.collections4.IteratorUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.register.core.*;
import uk.gov.register.serialization.*;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RegisterSerialisationFormatServiceTest {
    private static final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
    private final String EMPTY_REGISTER_ROOT_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Mock
    private RegisterContext registerContext;

    @Mock
    private Register register;

    private CommandParser commandParser;
    private RegisterSerialisationFormatService sutService;

    private Entry entry1;
    private Entry entry2;

    private Item item1;
    private Item item2;

    private RegisterProof emptyRegisterProof;

    @Before
    public void setUp() throws Exception {

        item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "item1sha"), jsonFactory.objectNode()
                .put("field-1", "entry1-field-1-value")
                .put("field-2", "entry1-field-2-value"));
        item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "item2sha"), jsonFactory.objectNode()
                .put("field-1", "entry2-field-1-value")
                .put("field-2", "entry2-field-2-value"));

        entry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "item1sha"), Instant.parse("2016-07-24T16:55:00Z"), "entry1-field-1-value");
        entry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "item2sha"), Instant.parse("2016-07-24T16:56:00Z"), "entry2-field-1-value");

        emptyRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, EMPTY_REGISTER_ROOT_HASH));

        when(registerContext.buildOnDemandRegister()).thenReturn(register);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Consumer<Register> callback = (Consumer<Register>) invocation.getArguments()[0];
                callback.accept(register);
                return null;
            }
        }).when(registerContext).transactionalRegisterOperation(any(Consumer.class));

        sutService = new RegisterSerialisationFormatService(registerContext);
        commandParser = new CommandParser();
    }

    @Test
    public void processRegisterComponents() throws Exception {
        when(register.getTotalEntries()).thenReturn(0).thenReturn(1);
        when(register.getRegisterProof()).thenReturn(emptyRegisterProof);

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(Arrays.asList(
                new AssertRootHashCommand(emptyRegisterProof),
                new AddItemCommand(item1),
                new AppendEntryCommand(entry1),
                new AppendEntryCommand(entry2)).iterator());

        InOrder inOrder = Mockito.inOrder(register);

        sutService.processRegisterComponents(rsf);

        verify(register, times(1)).getRegisterProof();
        verify(register, times(1)).putItem(item1);
        verify(register, times(2)).appendEntry(any());

        inOrder.verify(register, calls(1)).appendEntry(new Entry(1, entry1.getSha256hex(), entry1.getTimestamp(), entry1.getKey()));
        inOrder.verify(register, calls(1)).appendEntry(new Entry(2, entry2.getSha256hex(), entry2.getTimestamp(), entry2.getKey()));
    }

    @Test
    public void createRegisterSerialisationFormat_returnsRSFFromEntireRegister() throws NoSuchAlgorithmException {
        when(register.getItemIterator()).thenReturn(Arrays.asList(item1, item2).iterator());
        when(register.getEntryIterator()).thenReturn(Arrays.asList(entry1, entry2).iterator());

        RegisterProof expectedRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "1231234"));
        when(register.getRegisterProof()).thenReturn(expectedRegisterProof);

        RegisterSerialisationFormat actualRSF = sutService.createRegisterSerialisationFormat(register);
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        verify(register, times(1)).getItemIterator();
        verify(register, times(1)).getEntryIterator();
        verify(register, times(1)).getRegisterProof();

        AssertRootHashCommand expectedRootHashCommand = new AssertRootHashCommand(expectedRegisterProof);

        assertThat(actualCommands.size(), equalTo(6));
        assertThat(actualCommands, contains(
                new AssertRootHashCommand(emptyRegisterProof),
                new AddItemCommand(item1),
                new AddItemCommand(item2),
                new AppendEntryCommand(entry1),
                new AppendEntryCommand(entry2),
                expectedRootHashCommand));
    }

    @Test
    public void createRegisterSerialisationFormat_whenCalledWithBoundary_returnsPartialRSFRegister() {
        RegisterProof oneEntryRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "oneEntryInRegisterHash"));
        RegisterProof twoEntriesRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "twoEntriesInRegisterHash"));
        RegisterCommand assertRootOneEntryInRegister = new AssertRootHashCommand(oneEntryRegisterProof);
        RegisterCommand assertRootTwoEntriesInRegister = new AssertRootHashCommand(twoEntriesRegisterProof);

        when(register.getItemIterator(1, 2)).thenReturn(singletonList(item1).iterator());
        when(register.getEntryIterator(1, 2)).thenReturn(singletonList(entry2).iterator());
        when(register.getRegisterProof(1)).thenReturn(oneEntryRegisterProof);
        when(register.getRegisterProof(2)).thenReturn(twoEntriesRegisterProof);

        RegisterSerialisationFormat actualRSF = sutService.createRegisterSerialisationFormat(register, 1, 2);
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        verify(register, times(1)).getItemIterator(1, 2);
        verify(register, times(1)).getEntryIterator(1, 2);

        assertThat(actualCommands.size(), equalTo(4));
        assertThat(actualCommands, contains(
                assertRootOneEntryInRegister,
                new AddItemCommand(item1),
                new AppendEntryCommand(entry2),
                assertRootTwoEntriesInRegister));
    }

    @Test
    public void writeTo_writesEntireRSFtoStream() throws NoSuchAlgorithmException {
        when(register.getItemIterator()).thenReturn(Iterators.forArray(item1, item2));
        when(register.getEntryIterator()).thenReturn(Iterators.forArray(entry1, entry2));
        when(register.getRegisterProof()).thenReturn(new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "K3rfuFF1e")));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        sutService.writeTo(outputStream, commandParser);

        verify(register, times(1)).getItemIterator();
        verify(register, times(1)).getEntryIterator();

        String expectedRSF =
                "assert-root-hash\tsha-256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\n" +
                "add-item\t{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}\n" +
                "add-item\t{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}\n" +
                "append-entry\t2016-07-24T16:55:00Z\tsha-256:item1sha\tentry1-field-1-value\n" +
                "append-entry\t2016-07-24T16:56:00Z\tsha-256:item2sha\tentry2-field-1-value\n" +
                "assert-root-hash\tsha-256:K3rfuFF1e\n";

        String actualRSF = outputStream.toString();

        assertThat(actualRSF, Matchers.equalTo(expectedRSF));
    }

    @Test
    public void writeTo_whenCalledWithBoundary_writesPartialRSFtoStream() throws NoSuchAlgorithmException {
        when(register.getItemIterator(1, 2)).thenReturn(Iterators.forArray(item2));
        when(register.getEntryIterator(1, 2)).thenReturn(Iterators.forArray(entry2));
        when(register.getRegisterProof(1)).thenReturn(new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "K3rfuFF1e_uno")));
        when(register.getRegisterProof(2)).thenReturn(new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "K3rfuFF1e_dos")));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        sutService.writeTo(outputStream, commandParser, 1, 2);

        verify(register, times(1)).getItemIterator(1, 2);
        verify(register, times(1)).getEntryIterator(1, 2);

        String expectedRSF =
                "assert-root-hash\tsha-256:K3rfuFF1e_uno\n" +
                "add-item\t{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}\n" +
                "append-entry\t2016-07-24T16:56:00Z\tsha-256:item2sha\tentry2-field-1-value\n" +
                "assert-root-hash\tsha-256:K3rfuFF1e_dos\n";

        String actualRSF = outputStream.toString();

        assertThat(actualRSF, Matchers.equalTo(expectedRSF));
    }
}