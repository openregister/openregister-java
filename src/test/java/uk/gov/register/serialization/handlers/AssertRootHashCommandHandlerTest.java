package uk.gov.register.serialization.handlers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssertRootHashCommandHandlerTest {
    private AssertRootHashCommandHandler sutHandler;

    @Mock
    private Register register;

    @Mock
    private ProofGenerator proofGenerator;

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
        when(proofGenerator.getRootHash()).thenReturn(new HashValue(HashingAlgorithm.SHA256, "root-hash"));

        sutHandler.execute(assertRootHashCommand, register, proofGenerator);

        verify(proofGenerator, times(1)).getRootHash();
    }

    @Test (expected = RSFParseException.class)
    public void execute_failsIfRootHashesDontMatch() {
        when(proofGenerator.getRootHash()).thenReturn(new HashValue(HashingAlgorithm.SHA256, "different-hash"));

        assertExceptionThrown(assertRootHashCommand, "Exception when executing command: RegisterCommand");
    }

    @Test (expected = RSFParseException.class)
    public void execute_catchesExceptionsAndReturnsFailRSFResult() {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws IOException {
                throw new IOException("Forced exception");
            }
        }).when(proofGenerator).getRootHash();

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
            sutHandler.execute(command, register, proofGenerator);
        } catch (RSFParseException exception) {
            assertThat(exception.getMessage(), startsWith(exceptionMessage));
            throw exception;
        }
        fail("RSFParseException did not throw");
    }
}
