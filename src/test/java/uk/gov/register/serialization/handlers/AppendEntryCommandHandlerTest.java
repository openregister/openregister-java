package uk.gov.register.serialization.handlers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.serialization.RSFResult;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AppendEntryCommandHandlerTest {
    private AppendEntryCommandHandler sutHandler;

    @Mock
    private Register register;

    RegisterCommand appendEntryCommand;

    @Before
    public void setUp() throws Exception {
        sutHandler = new AppendEntryCommandHandler();
        appendEntryCommand = new RegisterCommand("append-entry", Arrays.asList("2016-07-24T16:55:00Z", "sha-256:item-sha", "entry1-field-1-value"));
    }

    @Test
    public void getCommandName_returnsCorrectCommandName() {
        assertThat(sutHandler.getCommandName(), equalTo("append-entry"));
    }

    @Test
    public void execute_appendsEntryToRegister() {
        when(register.getTotalEntries()).thenReturn(2);

        RSFResult rsfResult = sutHandler.execute(appendEntryCommand, register);

        Entry expectedEntry = new Entry(3, new HashValue(HashingAlgorithm.SHA256, "item-sha"), Instant.parse("2016-07-24T16:55:00Z"), "entry1-field-1-value");
        verify(register, times(1)).appendEntry(expectedEntry);
        assertThat(rsfResult, equalTo(RSFResult.createSuccessResult()));
    }

    @Test
    public void execute_catchesExceptionsAndReturnsFailRSFResult() {
        when(register.getTotalEntries()).thenThrow(Exception.class);

        RSFResult rsfResult = sutHandler.execute(appendEntryCommand, register);

        verify(register, never()).appendEntry(any());
        assertThat(rsfResult.isSuccessful(), equalTo(false));
    }

    @Test
    public void execute_failsForCommandWithInvalidArguments() {
        when(register.getTotalEntries()).thenReturn(2);
        RegisterCommand commandWithInvalidArguments = new RegisterCommand("append-entry", Arrays.asList("2016-07-T16:55:00Z", "sha-2tem-sha"));

        RSFResult rsfResult = sutHandler.execute(commandWithInvalidArguments, register);

        verify(register, never()).appendEntry(any());
        assertThat(rsfResult.isSuccessful(), equalTo(false));
        assertThat(rsfResult.getMessage(), startsWith("Exception when executing command: RegisterCommand"));
        assertThat(rsfResult.getDetails(), not(isEmptyOrNullString()));
    }

    @Test
    public void execute_failsForIncorrectCommandType() {
        RSFResult rsfResult = sutHandler.execute(new RegisterCommand("unknown-type", Arrays.asList("some", "data")), register);

        verify(register, never()).appendEntry(any());
        assertThat(rsfResult.isSuccessful(), equalTo(false));
        assertThat(rsfResult.getMessage(), equalTo("Incompatible handler (append-entry) and command type (unknown-type)"));
    }
}