package uk.gov.register.serialization.handlers;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterCommandContext;
import uk.gov.register.serialization.RegisterSerialisationFormat;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.register.core.HashingAlgorithm.SHA256;

@RunWith(MockitoJUnitRunner.class)
public class AppendEntryCommandHandlerTest {
    private AppendEntryCommandHandler sutHandler;

    @Mock
    private Register register;

    @Mock
    private RegisterCommandContext context;

    private RegisterCommand appendEntryCommand;

    private Instant july24 = Instant.parse("2016-07-24T16:55:00Z");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        sutHandler = new AppendEntryCommandHandler();
        appendEntryCommand = new RegisterCommand("append-entry", Arrays.asList("user", "entry1-field-1-value", "2016-07-24T16:55:00Z", "sha-256:item-sha"));
    }

    @Test
    public void getCommandName_returnsCorrectCommandName() {
        assertThat(sutHandler.getCommandName(), equalTo("append-entry"));
    }

    @Test
    public void execute_appendsEntryToRegisterV1() {
        when(context.getVersion()).thenReturn(RegisterSerialisationFormat.Version.V1);

        Item item = mock(Item.class);
        when(item.getBlobHash()).thenReturn(new HashValue(SHA256, "blob-sha"));
        when(register.getItemByV1Hash(new HashValue(SHA256, "item-sha"))).thenReturn(Optional.of(item));
        when(register.getTotalEntries(EntryType.user)).thenReturn(2);
        sutHandler.execute(appendEntryCommand, register, context);
        Entry expectedEntry = new Entry(3, new HashValue(SHA256, "item-sha"), new HashValue(SHA256, "blob-sha"), july24, "entry1-field-1-value", EntryType.user);
        verify(register, times(1)).appendEntry(expectedEntry);
    }

    @Test
    public void execute_appendsEntryToRegister() {

        when(context.getVersion()).thenReturn(RegisterSerialisationFormat.Version.V2);

        Item item = mock(Item.class);
        when(item.getSha256hex()).thenReturn(new HashValue(SHA256, "item-sha-old"));
        when(register.getItem(new HashValue(SHA256, "item-sha"))).thenReturn(Optional.of(item));
        when(register.getTotalEntries(EntryType.user)).thenReturn(2);
        sutHandler.execute(appendEntryCommand, register, context);
        Entry expectedEntry = new Entry(3, new HashValue(SHA256, "item-sha-old"), new HashValue(SHA256, "item-sha"), july24, "entry1-field-1-value", EntryType.user);
        verify(register, times(1)).appendEntry(expectedEntry);
    }

    @Test
    public void execute_appendsEntryToRegisterWithoutItemThrowsExceptionV1() {
        when(context.getVersion()).thenReturn(RegisterSerialisationFormat.Version.V1);
        expectedException.expect(RSFParseException.class);
        expectedException.expectMessage("Item not found for hash item-sha");
        sutHandler.execute(appendEntryCommand, register, context);
    }

    @Test
    public void execute_appendsEntryToRegisterWithoutItemThrowsException() {
        when(context.getVersion()).thenReturn(RegisterSerialisationFormat.Version.V2);
        expectedException.expect(RSFParseException.class);
        expectedException.expectMessage("Item not found for hash item-sha");
        sutHandler.execute(appendEntryCommand, register, context);
    }

    @Test (expected = RSFParseException.class)
    public void execute_catchesExceptionsAndReturnsFailRSFResultV1() {
        when(context.getVersion()).thenReturn(RegisterSerialisationFormat.Version.V1);

        Item item = mock(Item.class);
        when(item.getBlobHash()).thenReturn(new HashValue(SHA256, "blob-sha"));
        when(register.getItemByV1Hash(new HashValue(SHA256, "item-sha"))).thenReturn(Optional.of(item));
        doAnswer((Answer<Void>) invocation -> {
            throw new IOException("Forced exception");
        }).when(register).appendEntry(any(Entry.class));
        assertExceptionThrown(appendEntryCommand, "Exception when executing command: RegisterCommand");
    }

    @Test (expected = RSFParseException.class)
    public void execute_failsForCommandWithInvalidArguments() {
        RegisterCommand commandWithInvalidArguments = new RegisterCommand("append-entry", Arrays.asList("sha-2tem-sha", "2016-07-T16:55:00Z"));
        assertExceptionThrown(commandWithInvalidArguments, "Exception when executing command: RegisterCommand");
    }

    @Test (expected = RSFParseException.class)
    public void execute_failsForIncorrectCommandType() {
        RegisterCommand command = new RegisterCommand("unknown-type", Arrays.asList("some", "data"));
        assertExceptionThrown(command, "Incompatible handler (append-entry) and command type (unknown-type)");
    }

    private void assertExceptionThrown(RegisterCommand command, String exceptionMessage) throws RSFParseException {
        try {
            sutHandler.execute(command, register, context);
        } catch (RSFParseException exception) {
            assertThat(exception.getMessage(), startsWith(exceptionMessage));
            throw exception;
        }
        fail("RSFParseException did not throw");
    }
}
