package uk.gov.register.serialization;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.commons.collections4.IteratorUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.register.core.*;
import uk.gov.register.serialization.mappers.EntryToCommandMapper;
import uk.gov.register.serialization.mappers.ItemToCommandMapper;
import uk.gov.register.serialization.mappers.RootHashCommandMapper;
import uk.gov.register.util.HashValue;
import uk.gov.register.views.RegisterProof;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RSFCreatorTest {
    private static final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
    private static final String EMPTY_REGISTER_ROOT_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private final HashValue emptyRegisterHash = new HashValue(HashingAlgorithm.SHA256, EMPTY_REGISTER_ROOT_HASH);

    private RSFCreator sutCreator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Register register;

    private Entry systemEntry;
    private Entry entry1;
    private Entry entry2;
    private Item systemItem;
    private Item item1;
    private Item item2;

    private RegisterCommand assertEmptyRootHashCommand;
    private RegisterCommand addSystemItemCommand;
    private RegisterCommand addItem1Command;
    private RegisterCommand addItem2Command;
    private RegisterCommand appendSystemEntryCommand;
    private RegisterCommand appendEntry1Command;
    private RegisterCommand appendEntry2Command;

    @Before
    public void setUp() {
        sutCreator = new RSFCreator();
        sutCreator.register(new RootHashCommandMapper());
        sutCreator.register(new EntryToCommandMapper());
        sutCreator.register(new ItemToCommandMapper());

        systemItem = new Item(new HashValue(HashingAlgorithm.SHA256, "systemitemsha"), jsonFactory.objectNode()
                .put("system-field-1", "system-field-1-value")
                .put("system-field-2", "system-field-2-value"));
        item1 = new Item(new HashValue(HashingAlgorithm.SHA256, "item1sha"), jsonFactory.objectNode()
                .put("field-1", "entry1-field-1-value")
                .put("field-2", "entry1-field-2-value"));
        item2 = new Item(new HashValue(HashingAlgorithm.SHA256, "item2sha"), jsonFactory.objectNode()
                .put("field-1", "entry2-field-1-value")
                .put("field-2", "entry2-field-2-value"));

        systemEntry = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "systemitemsha"), Instant.parse("2016-07-24T16:54:00Z"), "system-key", EntryType.system);
        entry1 = new Entry(1, new HashValue(HashingAlgorithm.SHA256, "item1sha"), Instant.parse("2016-07-24T16:55:00Z"), "entry1-field-1-value", EntryType.user);
        entry2 = new Entry(2, new HashValue(HashingAlgorithm.SHA256, "item2sha"), Instant.parse("2016-07-24T16:56:00Z"), "entry2-field-1-value", EntryType.user);

        assertEmptyRootHashCommand = new RegisterCommand("assert-root-hash", Collections.singletonList(emptyRegisterHash.encode()));

        addSystemItemCommand  = new RegisterCommand("add-item", Collections.singletonList("{\"system-field-1\":\"system-field-1-value\",\"system-field-2\":\"system-field-2-value\"}"));
        addItem1Command = new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry1-field-1-value\",\"field-2\":\"entry1-field-2-value\"}"));
        addItem2Command = new RegisterCommand("add-item", Collections.singletonList("{\"field-1\":\"entry2-field-1-value\",\"field-2\":\"entry2-field-2-value\"}"));
        appendSystemEntryCommand = new RegisterCommand("append-entry", Arrays.asList("system", "system-key", "2016-07-24T16:54:00Z", "sha-256:systemitemsha"));
        appendEntry1Command = new RegisterCommand("append-entry", Arrays.asList("user", "entry1-field-1-value", "2016-07-24T16:55:00Z", "sha-256:item1sha"));
        appendEntry2Command = new RegisterCommand("append-entry", Arrays.asList("user", "entry2-field-1-value","2016-07-24T16:56:00Z", "sha-256:item2sha" ));
    }

    @Test
    public void createRegisterSerialisationFormat_returnsRSFFromEntireRegister() {
        when(register.getItemIterator(EntryType.user)).thenReturn(Arrays.asList(item1, item2).iterator());
        when(register.getItemIterator(EntryType.system)).thenReturn(Arrays.asList(systemItem).iterator());
        when(register.getEntryIterator(EntryType.system)).thenReturn(Arrays.asList(systemEntry).iterator());
        when(register.getEntryIterator(EntryType.user)).thenReturn(Arrays.asList(entry1, entry2).iterator());

        RegisterProof expectedRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "1231234"), 46464);
        when(register.getRegisterProof()).thenReturn(expectedRegisterProof);

        RegisterSerialisationFormat actualRSF = sutCreator.create(register);
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        verify(register, times(1)).getItemIterator(EntryType.user);
        verify(register, times(1)).getEntryIterator(EntryType.user);
        verify(register, times(1)).getRegisterProof();

        assertThat(actualCommands.size(), equalTo(8));
        assertThat(actualCommands, contains(
                assertEmptyRootHashCommand,
                addSystemItemCommand,
                addItem1Command,
                addItem2Command,
                appendSystemEntryCommand,
                appendEntry1Command,
                appendEntry2Command,
                new RegisterCommand("assert-root-hash", Collections.singletonList("sha-256:1231234"))
        ));
    }

    @Test
    public void createRegisterSerialisationFormat_whenCalledWithBoundary_returnsPartialRSFRegister() {
        RegisterProof oneEntryRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "oneEntryInRegisterHash"), 1);
        RegisterProof twoEntriesRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "twoEntriesInRegisterHash"), 2);

        when(register.getItemIterator(1, 2)).thenReturn(Collections.singletonList(item1).iterator());
        when(register.getEntryIterator(1, 2)).thenReturn(Collections.singletonList(entry1).iterator());
        when(register.getRegisterProof(1)).thenReturn(oneEntryRegisterProof);
        when(register.getRegisterProof(2)).thenReturn(twoEntriesRegisterProof);

        RegisterSerialisationFormat actualRSF = sutCreator.create(register, 1, 2);
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        verify(register, never()).getItemIterator(EntryType.system);
        verify(register, never()).getEntryIterator(EntryType.system);
        verify(register, times(1)).getItemIterator(1, 2);
        verify(register, times(1)).getEntryIterator(1, 2);

        assertThat(actualCommands.size(), equalTo(4));
        assertThat(actualCommands, contains(
                new RegisterCommand("assert-root-hash", Collections.singletonList(oneEntryRegisterProof.getRootHash().encode())),
                addItem1Command,
                appendEntry1Command,
                new RegisterCommand("assert-root-hash", Collections.singletonList(twoEntriesRegisterProof.getRootHash().encode()))
        ));
    }

    @Test
    public void createRegisterSerialisationFormat_whenStartIsZero_returnsSystemEntries() {
        RegisterProof twoEntriesRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "twoEntriesInRegisterHash"), 2);

        when(register.getItemIterator(EntryType.system)).thenReturn(Arrays.asList(systemItem).iterator());
        when(register.getItemIterator(0, 2)).thenReturn(Arrays.asList(item1, item2).iterator());
        when(register.getEntryIterator(EntryType.system)).thenReturn(Arrays.asList(systemEntry).iterator());
        when(register.getEntryIterator(0, 2)).thenReturn(Arrays.asList(entry1, entry2).iterator());

        when(register.getRegisterProof(2)).thenReturn(twoEntriesRegisterProof);

        RegisterSerialisationFormat actualRSF = sutCreator.create(register, 0, 2);
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        assertThat(actualCommands.size(), equalTo(8));
        assertThat(actualCommands, contains(
                assertEmptyRootHashCommand,
                addItem1Command,
                addItem2Command,
                addSystemItemCommand,
                appendSystemEntryCommand,
                appendEntry1Command,
                appendEntry2Command,
                new RegisterCommand("assert-root-hash", Collections.singletonList(twoEntriesRegisterProof.getRootHash().encode()))
        ));
    }

    @Test
    public void createRegisterSerialisationFormat_throwsAnExceptionForUnknownMapperType() throws Exception {
        when(register.getItemIterator(EntryType.user)).thenReturn(Arrays.asList(item1, item2).iterator());
        when(register.getEntryIterator(EntryType.system)).thenReturn(Collections.emptyIterator());
        when(register.getEntryIterator(EntryType.user)).thenReturn(Arrays.asList(entry1, entry2).iterator());
        when(register.getItemIterator(EntryType.system)).thenReturn(Arrays.asList(systemItem).iterator());

        RegisterProof expectedRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "1231234"), 28828);
        when(register.getRegisterProof()).thenReturn(expectedRegisterProof);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Mapper not registered for class: uk.gov.register.util.HashValue");

        RSFCreator creatorWithoutMappers = new RSFCreator();
        RegisterSerialisationFormat rsf = creatorWithoutMappers.create(register);
        IteratorUtils.toList(rsf.getCommands());
    }

    @Test
    public void createRegisterSerialisationFormat_whenParametersEqual_returnsOnlyRootHash() {
        RegisterProof twoEntriesRegisterProof = new RegisterProof(new HashValue(HashingAlgorithm.SHA256, "twoEntriesInRegisterHash"), 2);

        when(register.getRegisterProof(2)).thenReturn(twoEntriesRegisterProof);

        RegisterSerialisationFormat actualRSF = sutCreator.create(register, 2, 2);
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        assertThat(actualCommands.size(), equalTo(1));
        assertThat(actualCommands, contains(
                new RegisterCommand("assert-root-hash", Collections.singletonList(twoEntriesRegisterProof.getRootHash().encode()))
        ));
    }

    @Test
    public void createRegisterSerialisationFormat_whenParametersEqualAndZero_returnsSystemEntries() {
        when(register.getItemIterator(EntryType.system)).thenReturn(Arrays.asList(systemItem).iterator());
        when(register.getItemIterator(0, 0)).thenReturn(Collections.emptyIterator());
        when(register.getEntryIterator(EntryType.system)).thenReturn(Arrays.asList(systemEntry).iterator());
        when(register.getEntryIterator(0, 0)).thenReturn(Collections.emptyIterator());

        when(register.getRegisterProof(0)).thenReturn(new RegisterProof(emptyRegisterHash, 0));

        RegisterSerialisationFormat actualRSF = sutCreator.create(register, 0, 0);
        List<RegisterCommand> actualCommands = IteratorUtils.toList(actualRSF.getCommands());

        assertThat(actualCommands.size(), equalTo(4));
        assertThat(actualCommands, contains(
                assertEmptyRootHashCommand,
                addSystemItemCommand,
                appendSystemEntryCommand,
                assertEmptyRootHashCommand
        ));
    }
}
