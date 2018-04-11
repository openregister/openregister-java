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
import uk.gov.register.util.HashValue;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
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

        item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c"), jsonFactory.objectNode()
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
        when(assertRootHashHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());
        when(addItemHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());
        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(
                assertEmptyRootHashCommand,
                addItem1Command,
                addItem2Command,
                appendEntry1Command,
                assertMiddleRootHashCommand,
                appendEntry2Command,
                assertLastRootHashCommand).iterator());

        RegisterResult registerResult = sutExecutor.execute(rsf, register);

        InOrder inOrder = Mockito.inOrder(assertRootHashHandler, addItemHandler, appendEntryHandler);
        inOrder.verify(assertRootHashHandler, calls(1)).execute(assertEmptyRootHashCommand, register);
        inOrder.verify(addItemHandler, calls(1)).execute(addItem1Command, register);
        inOrder.verify(addItemHandler, calls(1)).execute(addItem2Command, register);
        inOrder.verify(appendEntryHandler, calls(1)).execute(appendEntry1Command, register);
        inOrder.verify(assertRootHashHandler, calls(1)).execute(assertMiddleRootHashCommand, register);
        inOrder.verify(appendEntryHandler, calls(1)).execute(appendEntry2Command, register);
        inOrder.verify(assertRootHashHandler, calls(1)).execute(assertLastRootHashCommand, register);

        assertThat(registerResult, equalTo(RegisterResult.createSuccessResult()));
    }

    @Test
    public void execute_failsForOrphanAppendEntry() {
        HashValue entry1hashValue = new HashValue(HashingAlgorithm.SHA256, "3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c");

        when(register.getItem(entry1hashValue)).thenReturn(Optional.empty());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(
                appendEntry1Command,
                addItem1Command).iterator());

        RegisterResult registerResult = sutExecutor.execute(rsf, register);

        verify(register, times(1)).getItem(entry1hashValue);

        assertThat(registerResult.isSuccessful(), equalTo(false));
        assertThat(registerResult.getMessage(), startsWith("Orphan append entry (line:1): RegisterCommand"));
    }

    @Test
    public void execute_succeedsWhenAppendEntryReferencesItemNotInRSFButInDB() {
        HashValue entry1hashValue = new HashValue(HashingAlgorithm.SHA256, "3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c");

        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());
        when(register.getItem(entry1hashValue)).thenReturn(Optional.of(item1));

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(singletonList(appendEntry1Command).iterator());
        RegisterResult registerResult = sutExecutor.execute(rsf, register);

        verify(register, times(1)).getItem(entry1hashValue);

        assertThat(registerResult, equalTo(RegisterResult.createSuccessResult()));
    }

    @Test
    public void execute_failsForOrphanAddItem() {
        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());
        when(addItemHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(
                addItem1Command,
                addItem2Command,
                appendEntry1Command).iterator());

        RegisterResult registerResult = sutExecutor.execute(rsf, register);

        assertThat(registerResult.isSuccessful(), equalTo(false));
        assertThat(registerResult.getMessage(), equalTo("Orphan add item (line:2): sha-256:1c7a3bbe9df447813863aead4a5ab7e3c20ffa59459df2540461c7d3de9d227a"));
    }

    @Test
    public void execute_failsForItemWhichWasntReferencedInRSF() {
        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());
        when(addItemHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(
                addItem1Command,
                appendEntry1Command,
                addItem1Command).iterator());

        RegisterResult registerResult = sutExecutor.execute(rsf, register);

        assertThat(registerResult.isSuccessful(), equalTo(false));
        assertThat(registerResult.getMessage(), equalTo("Orphan add item (line:3): sha-256:3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c"));
    }


    @Test
    public void execute_failsForUnknownCommand() {
        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(singletonList(
                new RegisterCommand("unknown-command", asList("some", "data"))).iterator());

        RegisterResult registerResult = sutExecutor.execute(rsf, register);

        assertThat(registerResult.isSuccessful(), equalTo(false));
        assertThat(registerResult.getMessage(), equalTo("Handler not registered for command: unknown-command"));
    }

    @Test
    public void execute_executingTheSameInvalidRSFGivesSameResult() {
        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());
        when(addItemHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(
                addItem1Command,
                appendEntry1Command,
                addItem1Command).iterator());

        // it has to be a new iterator because after 1st execution the iterator is going to be empty
        RegisterSerialisationFormat rsf2 = new RegisterSerialisationFormat(asList(
                addItem1Command,
                appendEntry1Command,
                addItem1Command).iterator());

        RegisterResult registerResult1 = sutExecutor.execute(rsf, register);
        RegisterResult registerResult2 = sutExecutor.execute(rsf2, register);

        assertThat(registerResult1, equalTo(registerResult2));
    }

    @Test
    public void shouldHandleMultiItemAddEntryCommands() {
        RegisterCommand addItem1 = new RegisterCommand("add-item", singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}"));
        String itemHash1 = "sha-256:3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c";
        RegisterCommand addItem2 = new RegisterCommand("add-item", singletonList("{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}"));
        String itemHash2 = "sha-256:1c7a3bbe9df447813863aead4a5ab7e3c20ffa59459df2540461c7d3de9d227a";
        RegisterCommand appendEntry = new RegisterCommand("append-entry",
                asList("user", "entry1-field-1-value", "2016-07-24T16:55:00Z", itemHash1 + ";" + itemHash2));

        when(addItemHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());
        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RegisterResult.createSuccessResult());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(asList(addItem1, addItem2, appendEntry).iterator());
        RegisterResult registerResult = sutExecutor.execute(rsf, register);

        InOrder inOrder = Mockito.inOrder(assertRootHashHandler, addItemHandler, appendEntryHandler);
        inOrder.verify(addItemHandler, calls(1)).execute(addItem1, register);
        inOrder.verify(addItemHandler, calls(1)).execute(addItem2, register);
        inOrder.verify(appendEntryHandler, calls(1)).execute(appendEntry, register);

        assertThat(registerResult, equalTo(RegisterResult.createSuccessResult()));
    }

}
