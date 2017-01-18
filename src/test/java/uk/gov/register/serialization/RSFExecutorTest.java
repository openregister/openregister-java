package uk.gov.register.serialization;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.commons.collections4.IteratorUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.register.core.*;
import uk.gov.register.serialization.handlers.AddItemCommandHandler;
import uk.gov.register.serialization.handlers.AppendEntryCommandHandler;
import uk.gov.register.serialization.handlers.AssertRootHashCommandHandler;
import uk.gov.register.serialization.mappers.EntryToCommandMapper;
import uk.gov.register.serialization.mappers.ItemToCommandMapper;
import uk.gov.register.serialization.mappers.RegisterProofCommandMapper;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RSFExecutorTest {
    private static final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
    private final String EMPTY_REGISTER_ROOT_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

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

    private RegisterProof emptyRegisterProof;
    private RegisterProof middleRegisterProof;
    private RegisterProof lastRegisterProof;

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

        emptyRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, EMPTY_REGISTER_ROOT_HASH));
        middleRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "middle-one"));
        lastRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "last-one"));

        assertEmptyRootHashCommand = new RegisterCommand("assert-root-hash", Collections.singletonList(emptyRegisterProof.getRootHash().encode()));
        assertMiddleRootHashCommand = new RegisterCommand("assert-root-hash", Collections.singletonList(middleRegisterProof.getRootHash().encode()));
        assertLastRootHashCommand = new RegisterCommand("assert-root-hash", Collections.singletonList(lastRegisterProof.getRootHash().encode()));

        addItem1Command = new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}"));
        addItem2Command = new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}"));
        appendEntry1Command = new RegisterCommand("append-entry", Arrays.asList("2016-07-24T16:55:00Z", "sha-256:3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c", "entry1-field-1-value"));
        appendEntry2Command = new RegisterCommand("append-entry", Arrays.asList("2016-07-24T16:56:00Z", "sha-256:1c7a3bbe9df447813863aead4a5ab7e3c20ffa59459df2540461c7d3de9d227a", "entry2-field-1-value"));
    }


    @Test
    public void execute_executesAllCommandsInCorrectOrder() {
        when(assertRootHashHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());
        when(addItemHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());
        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(Arrays.asList(
                assertEmptyRootHashCommand,
                addItem1Command,
                addItem2Command,
                appendEntry1Command,
                assertMiddleRootHashCommand,
                appendEntry2Command,
                assertLastRootHashCommand).iterator());

        RSFResult rsfResult = sutExecutor.execute(rsf, register);

        InOrder inOrder = Mockito.inOrder(assertRootHashHandler, addItemHandler, appendEntryHandler);
        inOrder.verify(assertRootHashHandler, calls(1)).execute(assertEmptyRootHashCommand, register);
        inOrder.verify(addItemHandler, calls(1)).execute(addItem1Command, register);
        inOrder.verify(addItemHandler, calls(1)).execute(addItem2Command, register);
        inOrder.verify(appendEntryHandler, calls(1)).execute(appendEntry1Command, register);
        inOrder.verify(assertRootHashHandler, calls(1)).execute(assertMiddleRootHashCommand, register);
        inOrder.verify(appendEntryHandler, calls(1)).execute(appendEntry2Command, register);
        inOrder.verify(assertRootHashHandler, calls(1)).execute(assertLastRootHashCommand, register);

        assertThat(rsfResult, equalTo(RSFResult.createSuccessResult()));
    }

    @Test
    public void execute_failsForOrphanAppendEntry() {
        HashValue entry1hashValue = new HashValue(HashingAlgorithm.SHA256, "3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c");

        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());
        when(addItemHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());
        when(register.getItemBySha256(entry1hashValue)).thenReturn(Optional.empty());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(Arrays.asList(
                appendEntry1Command,
                addItem1Command).iterator());

        RSFResult rsfResult = sutExecutor.execute(rsf, register);

        verify(register, times(1)).getItemBySha256(entry1hashValue);

        assertThat(rsfResult.isSuccessful(), equalTo(false));
        assertThat(rsfResult.getMessage(), startsWith("Orphan append entry (line:1): RegisterCommand"));
    }

    @Test
    public void execute_succeedsWhenAppendEntryReferencesItemNotInRSFButInDB() {
        HashValue entry1hashValue = new HashValue(HashingAlgorithm.SHA256, "3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c");

        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());
        when(addItemHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());
        when(register.getItemBySha256(entry1hashValue)).thenReturn(Optional.of(item1));

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(Collections.singletonList(appendEntry1Command).iterator());
        RSFResult rsfResult = sutExecutor.execute(rsf, register);

        verify(register, times(1)).getItemBySha256(entry1hashValue);

        assertThat(rsfResult, equalTo(RSFResult.createSuccessResult()));
    }

    @Test
    public void execute_failsForOrphanAddItem() {
        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());
        when(addItemHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(Arrays.asList(
                addItem1Command,
                addItem2Command,
                appendEntry1Command).iterator());

        RSFResult rsfResult = sutExecutor.execute(rsf, register);

        assertThat(rsfResult.isSuccessful(), equalTo(false));
        assertThat(rsfResult.getMessage(), equalTo("Orphan add item (line:2): sha-256:1c7a3bbe9df447813863aead4a5ab7e3c20ffa59459df2540461c7d3de9d227a"));
    }

    @Test
    public void execute_failsForItemWhichWasntReferencedInRSF() {
        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());
        when(addItemHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(Arrays.asList(
                addItem1Command,
                appendEntry1Command,
                addItem1Command).iterator());

        RSFResult rsfResult = sutExecutor.execute(rsf, register);

        assertThat(rsfResult.isSuccessful(), equalTo(false));
        assertThat(rsfResult.getMessage(), equalTo("Orphan add item (line:3): sha-256:3b0c026a0197e3f6392940a7157e0846028f55c3d3db6b6e9b3400fea4a9612c"));
    }


    @Test
    public void execute_failsForUnknownCommand() {
        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(Collections.singletonList(
                new RegisterCommand("unknown-command", Arrays.asList("some", "data"))).iterator());

        RSFResult rsfResult = sutExecutor.execute(rsf, register);

        assertThat(rsfResult.isSuccessful(), equalTo(false));
        assertThat(rsfResult.getMessage(), equalTo("Handler not registered for command: unknown-command"));
    }

    @Test
    public void execute_executingTheSameInvalidRSFGivesSameResult() {
        when(appendEntryHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());
        when(addItemHandler.execute(any(), eq(register))).thenReturn(RSFResult.createSuccessResult());

        RegisterSerialisationFormat rsf = new RegisterSerialisationFormat(Arrays.asList(
                addItem1Command,
                appendEntry1Command,
                addItem1Command).iterator());

        // it has to be a new iterator because after 1st execution the iterator is going to be empty
        RegisterSerialisationFormat rsf2 = new RegisterSerialisationFormat(Arrays.asList(
                addItem1Command,
                appendEntry1Command,
                addItem1Command).iterator());

        RSFResult rsfResult1 = sutExecutor.execute(rsf, register);
        RSFResult rsfResult2 = sutExecutor.execute(rsf2, register);

        assertThat(rsfResult1, equalTo(rsfResult2));
    }
}