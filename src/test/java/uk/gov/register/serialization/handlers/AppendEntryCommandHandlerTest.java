package uk.gov.register.serialization.handlers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.serialization.RegisterResult;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;
import static uk.gov.register.core.HashingAlgorithm.SHA256;

@RunWith(MockitoJUnitRunner.class)
public class AppendEntryCommandHandlerTest {
    private AppendEntryCommandHandler sutHandler;

    @Mock
    private Register register;

    private RegisterCommand appendEntryCommand;
    private Instant july24 = Instant.parse("2016-07-24T16:55:00Z");

    @Before
    public void setUp() throws Exception {
        sutHandler = new AppendEntryCommandHandler();
        appendEntryCommand = new RegisterCommand("append-entry", Arrays.asList("entry1-field-1-value", "2016-07-24T16:55:00Z", "sha-256:item-sha"));
    }

    @Test
    public void getCommandName_returnsCorrectCommandName() {
        assertThat(sutHandler.getCommandName(), equalTo("append-entry"));
    }

    @Test
    public void execute_appendsEntryToRegister() {
        when(register.getTotalEntries()).thenReturn(2);

        RegisterResult registerResult = sutHandler.execute(appendEntryCommand, register);


        Entry expectedEntry = new Entry(3, new HashValue(SHA256, "item-sha"), july24, "entry1-field-1-value");
        verify(register, times(1)).appendEntry(expectedEntry);
        assertThat(registerResult, equalTo(RegisterResult.createSuccessResult()));
    }

    @Test
    public void execute_appendsMultiItemEntryToRegister() {
        when(register.getTotalEntries()).thenReturn(2);

        RegisterCommand command = new RegisterCommand("append-entry",
                Arrays.asList("entry1-field-1-value", "2016-07-24T16:55:00Z", "sha-256:aaa;sha-256:bbb"));
        RegisterResult registerResult = sutHandler.execute(command, register);

        Entry expectedEntry = new Entry(3, Arrays.asList(new HashValue(SHA256, "aaa"),
                new HashValue(SHA256, "bbb")), july24, "entry1-field-1-value");
        verify(register, times(1)).appendEntry(expectedEntry);
        assertThat(registerResult, equalTo(RegisterResult.createSuccessResult()));
    }

    @Test
    public void execute_appendsZeroItemEntryToRegister() {
        when(register.getTotalEntries()).thenReturn(2);

        RegisterCommand command = new RegisterCommand("append-entry",
                Arrays.asList("entry1-field-1-value", "2016-07-24T16:55:00Z", ""));
        RegisterResult registerResult = sutHandler.execute(command, register);

        Entry expectedEntry = new Entry(3, new ArrayList<>(), july24, "entry1-field-1-value");
        verify(register, times(1)).appendEntry(expectedEntry);
        assertThat(registerResult, equalTo(RegisterResult.createSuccessResult()));
    }


    @Test
    public void execute_catchesExceptionsAndReturnsFailRSFResult() {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws IOException {
                throw new IOException("Forced exception");
            }
        }).when(register).appendEntry(any(Entry.class));

        RegisterResult registerResult = sutHandler.execute(appendEntryCommand, register);

        assertThat(registerResult.isSuccessful(), equalTo(false));
        assertThat(registerResult.getMessage(), startsWith("Exception when executing command: RegisterCommand"));
        assertThat(registerResult.getDetails(), not(isEmptyOrNullString()));
    }

    @Test
    public void execute_failsForCommandWithInvalidArguments() {
        when(register.getTotalEntries()).thenReturn(2);
        RegisterCommand commandWithInvalidArguments = new RegisterCommand("append-entry", Arrays.asList("sha-2tem-sha", "2016-07-T16:55:00Z"));

        RegisterResult registerResult = sutHandler.execute(commandWithInvalidArguments, register);

        verify(register, never()).appendEntry(any());
        assertThat(registerResult.isSuccessful(), equalTo(false));
        assertThat(registerResult.getMessage(), startsWith("Exception when executing command: RegisterCommand"));
        assertThat(registerResult.getDetails(), not(isEmptyOrNullString()));
    }

    @Test
    public void execute_failsForIncorrectCommandType() {
        RegisterResult registerResult = sutHandler.execute(new RegisterCommand("unknown-type", Arrays.asList("some", "data")), register);

        verify(register, never()).appendEntry(any());
        assertThat(registerResult.isSuccessful(), equalTo(false));
        assertThat(registerResult.getMessage(), equalTo("Incompatible handler (append-entry) and command type (unknown-type)"));
    }
}
