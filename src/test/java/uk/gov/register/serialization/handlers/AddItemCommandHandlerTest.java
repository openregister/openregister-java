package uk.gov.register.serialization.handlers;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.serialization.RegisterResult;
import uk.gov.register.serialization.RegisterCommand;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AddItemCommandHandlerTest {
    private static final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;

    private AddItemCommandHandler sutHandler;

    @Mock
    private Register register;

    @Mock
    private ProofGenerator proofGenerator;

    private RegisterCommand addItemCommand;

    @Before
    public void setUp() throws Exception {
        sutHandler = new AddItemCommandHandler();
        addItemCommand = new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}"));
    }

    @Test
    public void getCommandName_returnsCorrectCommandName() {
        assertThat(sutHandler.getCommandName(), equalTo("add-item"));
    }

    @Test
    public void execute_addsItemToRegister() {
        sutHandler.execute(addItemCommand, register, proofGenerator);

        Item expectedItem = new Item(new HashValue(HashingAlgorithm.SHA256, "3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c"), new HashValue(HashingAlgorithm.SHA256, "bc7242dd795173a3632f3385b8ecd4f5b37a10130925b9c2aadfafbfc73a19c4"), jsonFactory.objectNode()
                .put("field-1", "entry1-field-1-value")
                .put("field-2", "entry1-field-2-value"));

        verify(register, times(1)).addItem(expectedItem);
    }

    @Test (expected = RSFParseException.class)
    public void execute_catchesExceptionsAndReturnsFailRSFResult() {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws IOException {
                throw new IOException("Forced exception");
            }
        }).when(register).addItem(any(Item.class));

        assertExceptionThrown(addItemCommand, "Exception when executing command: RegisterCommand");
    }

    @Test (expected = RSFParseException.class)
    public void execute_failsForCommandWithInvalidArguments() {
        RegisterCommand commandWithInvalidArguments = new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry1-field-1-value"));
        assertExceptionThrown(commandWithInvalidArguments, "Exception when executing command: RegisterCommand");
    }

    @Test (expected = RSFParseException.class)
    public void execute_failsForIncorrectCommandType() {
        RegisterCommand command = new RegisterCommand("unknown-type", Arrays.asList("some", "data"));
        assertExceptionThrown(command, "Incompatible handler (add-item) and command type (unknown-type)");
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
