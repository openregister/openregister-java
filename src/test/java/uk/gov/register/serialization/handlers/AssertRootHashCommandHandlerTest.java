package uk.gov.register.serialization.handlers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.serialization.RegisterResult;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AssertRootHashCommandHandlerTest {
    private AssertRootHashCommandHandler sutHandler;

    @Mock
    private Register register;

    private RegisterCommand assertRootHashCommand;

    @Before
    public void setUp() throws Exception {
        sutHandler = new AssertRootHashCommandHandler();
        assertRootHashCommand = new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:root-hash"));
    }

    @Test
    public void getCommandName_returnsCorrectCommandName() {
        assertThat(sutHandler.getCommandName(), equalTo("assert-root-hash"));
    }

    @Test
    public void execute_obtainsAndAssertsRegisterProof() {
        RegisterProof registerProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "root-hash"), 123);
        when(register.getRegisterProof()).thenReturn(registerProof);

        sutHandler.execute(assertRootHashCommand, register);

        verify(register, times(1)).getRegisterProof();
    }

    @Test (expected = RSFParseException.class)
    public void execute_failsIfRootHashesDontMatch() {
        RegisterProof registerProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "different-hash"), 456);
        when(register.getRegisterProof()).thenReturn(registerProof);

        assertExceptionThrown(assertRootHashCommand, "Exception when executing command: RegisterCommand");
    }

    @Test (expected = RSFParseException.class)
    public void execute_catchesExceptionsAndReturnsFailRSFResult() {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws IOException {
                throw new IOException("Forced exception");
            }
        }).when(register).getRegisterProof();

        assertExceptionThrown(assertRootHashCommand, "Exception when executing command: RegisterCommand");
    }

    @Test (expected = RSFParseException.class)
    public void execute_failsForCommandWithInvalidArguments() {
        RegisterCommand commandWithInvalidArguments = new RegisterCommand("assert-root-hash", Collections.singletonList("sha2-2:43534"));
        assertExceptionThrown(commandWithInvalidArguments, "Exception when executing command: RegisterCommand");
    }

    @Test (expected = RSFParseException.class)
    public void execute_failsForIncorrectCommandType() {
        RegisterCommand command = new RegisterCommand("unknown-type", Arrays.asList("some", "data"));
        assertExceptionThrown(command, "Incompatible handler (assert-root-hash) and command type (unknown-type)");
    }

    private void assertExceptionThrown(RegisterCommand command, String exceptionMessage) throws RSFParseException {
        try {
            sutHandler.execute(command, register);
        } catch (RSFParseException exception) {
            assertThat(exception.getMessage(), startsWith(exceptionMessage));
            throw exception;
        }
        fail("RSFParseException did not throw");
    }
}
