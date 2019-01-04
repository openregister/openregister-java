package uk.gov.register.serialization;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.exceptions.RSFParseException;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.util.HashValue;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RSFExecutorTest {
    private static final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
    private static final String EMPTY_REGISTER_ROOT_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private RSFExecutor sutExecutor;

    @Mock
    private Register register;

    @Mock
    private RegisterCommandHandler assertRootHashHandler;

    @Mock
    private RegisterCommandHandler appendEntryHandler;

    @Mock
    private RegisterCommandHandler addItemHandler;

    @Mock
    private ProofGenerator proofGenerator;

    private Item item1;

    private RegisterCommand assertEmptyRootHashCommand;
    private RegisterCommand addItem1Command;
    private RegisterCommand addItem2Command;
    private RegisterCommand appendEntry1Command;
    private RegisterCommand appendEntry2Command;
    private RegisterCommand assertMiddleRootHashCommand;
    private RegisterCommand assertLastRootHashCommand;

    @Before
    public void setUp() {
        when(assertRootHashHandler.getCommandName()).thenReturn("assert-root-hash");
        when(appendEntryHandler.getCommandName()).thenReturn("append-entry");
        when(addItemHandler.getCommandName()).thenReturn("add-item");

        sutExecutor = new RSFExecutor();
        sutExecutor.register(assertRootHashHandler);
        sutExecutor.register(appendEntryHandler);
        sutExecutor.register(addItemHandler);

        item1 = new Item(jsonFactory.objectNode()
                .put("field-1", "entry1-field-1-value")
                .put("field-2", "entry1-field-2-value"));

        HashValue emptyRootHash = new HashValue(HashingAlgorithm.SHA256, EMPTY_REGISTER_ROOT_HASH);
        HashValue middleRootHash = new HashValue(HashingAlgorithm.SHA256, "middle-one");
        HashValue lastRootHash = new HashValue(HashingAlgorithm.SHA256, "last-one");

        assertEmptyRootHashCommand = new RegisterCommand("assert-root-hash", singletonList(emptyRootHash.encode()));
        assertMiddleRootHashCommand = new RegisterCommand("assert-root-hash", singletonList(middleRootHash.encode()));
        assertLastRootHashCommand = new RegisterCommand("assert-root-hash", singletonList(lastRootHash.encode()));

        addItem1Command = new RegisterCommand("add-item", singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}"));
        addItem2Command = new RegisterCommand("add-item", singletonList("{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}"));
        appendEntry1Command = new RegisterCommand("append-entry", asList("user", "entry1-field-1-value", "2016-07-24T16:55:00Z", "sha-256:3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c"));
        appendEntry2Command = new RegisterCommand("append-entry", asList("user", "entry2-field-1-value", "2016-07-24T16:56:00Z", "sha-256:1c7a3bbe9df447813863aead4a5ab7e3c20ffa59459df2540461c7d3de9d227a"));
    }


    @Test
    public void execute_executesAllCommandsInCorrectOrder() {
        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(
                assertEmptyRootHashCommand,
                addItem1Command,
                addItem2Command,
                appendEntry1Command,
                assertMiddleRootHashCommand,
                appendEntry2Command,
                assertLastRootHashCommand).iterator());


        sutExecutor.execute(rsf, register, proofGenerator);

        InOrder inOrder = Mockito.inOrder(assertRootHashHandler, addItemHandler, appendEntryHandler);
        inOrder.verify(assertRootHashHandler, calls(1)).execute(assertEmptyRootHashCommand, register, proofGenerator);
        inOrder.verify(addItemHandler, calls(1)).execute(addItem1Command, register, proofGenerator);
        inOrder.verify(addItemHandler, calls(1)).execute(addItem2Command, register, proofGenerator);
        inOrder.verify(appendEntryHandler, calls(1)).execute(appendEntry1Command, register, proofGenerator);
        inOrder.verify(assertRootHashHandler, calls(1)).execute(assertMiddleRootHashCommand, register, proofGenerator);
        inOrder.verify(appendEntryHandler, calls(1)).execute(appendEntry2Command, register, proofGenerator);
        inOrder.verify(assertRootHashHandler, calls(1)).execute(assertLastRootHashCommand, register, proofGenerator);
    }

    @Test (expected = RSFParseException.class)
    public void execute_failsForOrphanAppendEntry() {
        HashValue entry1hashValue = new HashValue(HashingAlgorithm.SHA256, "3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c");

        when(register.getItemByV1Hash(entry1hashValue)).thenReturn(Optional.empty());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(
                appendEntry1Command,
                addItem1Command).iterator());

        assertExceptionThrown(rsf, "Orphan append entry (line:1): RegisterCommand");
    }

    @Test
    public void execute_succeedsWhenAppendEntryReferencesItemNotInRSFButInDB() {
        HashValue entry1hashValue = new HashValue(HashingAlgorithm.SHA256, "3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c");

        when(register.getItemByV1Hash(entry1hashValue)).thenReturn(Optional.of(item1));

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(singletonList(appendEntry1Command).iterator());
        sutExecutor.execute(rsf, register, proofGenerator);

        verify(register, times(1)).getItemByV1Hash(entry1hashValue);
    }

    @Test (expected = RSFParseException.class)
    public void execute_failsForOrphanAddItem() {
        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(
                addItem1Command,
                addItem2Command,
                appendEntry1Command).iterator());

        assertExceptionThrown(rsf, "Orphan add item (line:2): sha-256:1c7a3bbe9df447813863aead4a5ab7e3c20ffa59459df2540461c7d3de9d227a");
    }

    @Test (expected = RSFParseException.class)
    public void execute_failsForItemWhichWasntReferencedInRSF() {
        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(
                addItem1Command,
                appendEntry1Command,
                addItem1Command).iterator());

        assertExceptionThrown(rsf, "Orphan add item (line:3): sha-256:3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c");
    }


    @Test (expected = RSFParseException.class)
    public void execute_failsForUnknownCommand() {
        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(singletonList(
                new RegisterCommand("unknown-command", asList("some", "data"))).iterator());

        assertExceptionThrown(rsf, "Handler not registered for command: unknown-command");
    }

    @Test (expected = RSFParseException.class)
    public void execute_executingTheSameInvalidRSFGivesSameResult() {
        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(
                addItem1Command,
                appendEntry1Command,
                addItem1Command).iterator());

        // it has to be a new iterator because after 1st execution the iterator is going to be empty
        RegisterSerialisationFormat rsf2 = new RegisterSerialisationFormat(asList(
                addItem1Command,
                appendEntry1Command,
                addItem1Command).iterator());

        try {
            sutExecutor.execute(rsf, register, proofGenerator);
        } catch (RSFParseException exception1) {
            try {
                sutExecutor.execute(rsf2, register, proofGenerator);
            } catch (RSFParseException exception2) {
                assertThat(exception1.getMessage(), equalTo(exception2.getMessage()));
                throw exception2;
            }
        }
        fail("RSFParseException did not throw");
    }

    @Test
    public void shouldHandleMultiItemAddEntryCommands() {
        RegisterCommand addItem1 = new RegisterCommand("add-item", singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}"));
        String itemHash1 = "sha-256:3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c";
        RegisterCommand addItem2 = new RegisterCommand("add-item", singletonList("{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}"));
        String itemHash2 = "sha-256:1c7a3bbe9df447813863aead4a5ab7e3c20ffa59459df2540461c7d3de9d227a";
        RegisterCommand appendEntry = new RegisterCommand("append-entry",
                asList("user", "entry1-field-1-value", "2016-07-24T16:55:00Z", itemHash1 + ";" + itemHash2));

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(addItem1, addItem2, appendEntry).iterator());
        sutExecutor.execute(rsf, register, proofGenerator);

        InOrder inOrder = Mockito.inOrder(assertRootHashHandler, addItemHandler, appendEntryHandler);
        inOrder.verify(addItemHandler, calls(1)).execute(addItem1, register, proofGenerator);
        inOrder.verify(addItemHandler, calls(1)).execute(addItem2, register, proofGenerator);
        inOrder.verify(appendEntryHandler, calls(1)).execute(appendEntry, register, proofGenerator);
    }

    private void assertExceptionThrown(RegisterSerialisationFormat rsf, String exceptionMessage) throws RSFParseException {
        try {
            sutExecutor.execute(rsf, register, proofGenerator);
        } catch (RSFParseException exception) {
            assertThat(exception.getMessage(), startsWith(exceptionMessage));
            throw exception;
        }
        fail("RSFParseException did not throw");
    }
}
